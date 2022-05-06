package jp.nulab.nulab_exam.controller;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.slack.api.Slack;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jp.nulab.nulab_exam.bean.dto.BlockKitAccessoryDto;
import jp.nulab.nulab_exam.bean.dto.BlockKitBlockDto;
import jp.nulab.nulab_exam.bean.dto.BlockKitOptionDto;
import jp.nulab.nulab_exam.bean.dto.BlockKitPlaceholderDto;
import jp.nulab.nulab_exam.bean.dto.BlockKitTextDto;
import jp.nulab.nulab_exam.bean.dto.NulabIssueCountDto;
import jp.nulab.nulab_exam.bean.dto.NulabIssueDto;
import jp.nulab.nulab_exam.bean.dto.NulabIssuesDto;
import jp.nulab.nulab_exam.bean.dto.NulabProjectDto;

@RestController
@EnableAutoConfiguration
public class NulabExamController {

    // システム環境変数に登録したユーザー情報を取得する
    final String domainName = System.getenv("BACKLOG_DOMAIN_NAME");
    final String apiKey = System.getenv("BACKLOG_API_KEY");
    final String token = System.getenv("SLACK_BOT_TOKEN");

    Gson gson = new Gson();
    RestTemplate rest = new RestTemplate();

    /**
     * Slack Event時処理
     * @param header ヘッダー情報
     * @param data ボディ情報
     * @return JsonObject Slack APIのレスポンス用
     * @author　Karin Nunokawa
     */
    @RequestMapping(method = RequestMethod.POST, value = "/event")
    @SuppressWarnings("unchecked")
    public JsonObject event(@RequestHeader(name="X-Slack-Retry-Num",required=false)String header, @RequestBody String data) throws Exception {

        // 3秒以内にレスポンスがない限り複数回のリクエストが送られてくるため、最初のリクエスト以外に関しては処理を行わない
        JsonObject returnJson = new JsonObject();
        if(!(header == null)){
            returnJson.addProperty("statusCode", 200);
            returnJson.addProperty("body", "No need to resend");
            return returnJson;
        }

        // パラメータをMAP形式に変更
        Map<String, Object> argsMap = this.stringToMap(data);

        switch (argsMap.get("type").toString()) {
            // Slack APIのEvent Subscriptions用
            case "url_verification":
                returnJson.addProperty("challenge", argsMap.get("challenge").toString());
                return returnJson;

            // 実際にメッセージが送信されたときの処理
            case "event_callback":
                // パラメータから必要なデータを取得する
                Object event = argsMap.get("event");
                Map<String, String> eventMap = new ObjectMapper().convertValue(event, Map.class);
                String text = eventMap.get("text");
                String channel = eventMap.get("channel");

                // チャンネルに送られたメッセージが"課題の完了"の場合、未完了の課題の一覧を取得する
                if(text.equals("課題の完了")){

                    // 現在アクティブなプロジェクト一覧の取得
                    NulabProjectDto[] projectsResponse = this.getProjects();

                    // 課題一覧取得用にプロジェクトIDを取得する
                    StringBuilder sb = this.getProjectId(projectsResponse);

                    // 課題一覧の取得
                    NulabIssueDto[] issuesResponse = this.getIssues(sb.toString());

                    // 課題Keyを全て格納
                    List<Map<String,String>> issueMapList = new ArrayList<>();

                    // 取得した課題一覧の課題Keyのみ取り出す
                    for (NulabIssueDto issue : issuesResponse) {
                        // 状態が完了以外の課題のみ取り出す
                        if(issue.getStatus().getId() != 4){
                            // 更新に必要なデータを格納
                            Map<String,String> issueMap = new HashMap<>();
                            issueMap.put("issueKey", issue.getIssueKey());
                            issueMap.put("summary", issue.getSummary());
                            issueMapList.add(issueMap);
                        }
                    }

                    // BlockKitに必要なDtoを生成する
                    // text
                    BlockKitTextDto textDto = new BlockKitTextDto();

                    textDto.setType("mrkdwn");
                    textDto.setText("完了する課題を選択してください");

                    // placeholder
                    BlockKitPlaceholderDto placeholderDto = new BlockKitPlaceholderDto();

                    placeholderDto.setType("plain_text");
                    placeholderDto.setText("Select an item");
                    placeholderDto.setEmoji(true);

                    // optionを格納するoptionsを生成
                    List<BlockKitOptionDto> options = new ArrayList<>();
                    // 課題数分だけoptionsを設定する（プルダウンメニュー）
                    for(int i=0; i< issueMapList.size(); i++){
                        BlockKitOptionDto optionDto = new BlockKitOptionDto();
                        BlockKitPlaceholderDto optionTextDto = new BlockKitPlaceholderDto();

                        optionTextDto.setType("plain_text");
                        optionTextDto.setText(issueMapList.get(i).get("summary"));
                        optionTextDto.setEmoji(true);

                        optionDto.setText(optionTextDto);
                        optionDto.setValue(issueMapList.get(i).get("issueKey"));

                        options.add(optionDto);
                    }

                    // accessory
                    BlockKitAccessoryDto accessoryDto = new BlockKitAccessoryDto();

                    accessoryDto.setType("static_select");
                    accessoryDto.setPlaceholder(placeholderDto);
                    accessoryDto.setOptions(options);
                    accessoryDto.setAction_id("static_select-action");

                    // block
                    BlockKitBlockDto blockDto = new BlockKitBlockDto();
                    blockDto.setType("section");
                    blockDto.setText(textDto);
                    blockDto.setAccessory(accessoryDto);

                    String blockText = gson.toJson(blockDto);

                    // インスタンスの生成
                    Slack slack = Slack.getInstance();

                    slack.methods(token).chatPostMessage(req -> req
                    .channel(channel)
                    .blocksAsString("["+blockText+"]")
                    );

                // 課題一覧を返却する
                } else if (text.equals("課題一覧の取得")) {

                    // 現在アクティブなプロジェクト一覧の取得
                    NulabProjectDto[] projectsResponse = this.getProjects();

                    // 課題一覧取得用にプロジェクトIDを取得する
                    StringBuilder sb = this.getProjectId(projectsResponse);

                    // メッセージテキスト用にプロジェクトIDとプロジェクト名を取り出す
                    Map<String, String> projectMap = new HashMap<>();
                    List<Map<String, String>> projectMapList = new ArrayList<>();
                    for (NulabProjectDto project : projectsResponse) {
                        projectMap.put("projectId", Integer.toString(project.getId()));
                        projectMap.put("name", project.getName());
                        projectMapList.add(projectMap);
                    }

                    // 課題一覧の取得
                    NulabIssueDto[] issuesResponse = this.getIssues(sb.toString());

                    // 取得した課題一覧を1つずつ取り出し、メッセージを作成する
                    int count = 1;
                    StringBuilder sbMessage = new StringBuilder();
                    for (NulabIssueDto issue : issuesResponse) {
                        String projectName = "";
                        for(int i=0;i < projectMapList.size(); i++){
                            if(projectMapList.get(i).get("projectId").equals(Integer.toString(issue.getProjectId()))){
                                projectName = projectMapList.get(i).get("name");
                            }
                        }
                        sbMessage.append(count+". "+issue.getSummary()+"（"+ projectName +"）　【状態："+ issue.getStatus().getName() + "】\n");
                        count++;
                    }
                    // インスタンスの生成
                    Slack slack = Slack.getInstance();

                    slack.methods(token).chatPostMessage(req -> req
                    .channel(channel)
                    .text(sbMessage.toString())
                    );
                } else if(text.equals("課題数の取得")){

                    // 現在アクティブなプロジェクト一覧の取得
                    NulabProjectDto[] projectsResponse = this.getProjects();

                    // 課題数の取得用にプロジェクトIDを取得する
                    StringBuilder sb = this.getProjectId(projectsResponse);

                    // 課題数の取得処理
                    NulabIssueCountDto issueCountResponse = this.getIssueCount(sb.toString());
                    // インスタンスの生成
                    Slack slack = Slack.getInstance();

                    slack.methods(token).chatPostMessage(req -> req
                    .channel(channel)
                    .text("状態が未完了の課題数："+Integer.toString(issueCountResponse.getCount()))
                    );
                }

                return returnJson;
            default:
                throw new Exception();
        }
    }

    /**
     * Block Kit選択時処理
     * @param header ヘッダー情報
     * @param data ボディ情報
     * @return JsonObject Slack APIのレスポンス用
     * @author　Karin Nunokawa
     */
    @RequestMapping(method = RequestMethod.POST, value = "/blockActions")
    @SuppressWarnings("unchecked")
    public JsonObject blockActions(@RequestHeader(name="X-Slack-Retry-Num",required=false)String header, @RequestBody String data) throws Exception {

        // 3秒以内にレスポンスがない限り複数回のリクエストが送られてくるため、最初のリクエスト以外に関しては処理を行わない
        JsonObject returnJson = new JsonObject();
        if(!(header == null)){
            returnJson.addProperty("statusCode", 200);
            returnJson.addProperty("body", "No need to resend");
            return returnJson;
        }

        // コールバック時は不要な文字列がデータの最初に含まれるため取り除く
        data = data.replace("payload=", "");
        data = URLDecoder.decode(data,"UTF-8");

        Map<String, Object> argsMap = this.stringToMap(data);

        switch (argsMap.get("type").toString()) {

            case "block_actions":

                // チャンネルIDを取得する
                Map<String, Object> channelMap = new ObjectMapper().convertValue(argsMap.get("channel"), Map.class);
                String channel = channelMap.get("id").toString();

                // 必要な情報をMapに変換する
                Map<String, Object> stateMap = new ObjectMapper().convertValue(argsMap.get("state"), Map.class);
                Map<String, Object> valuesMap = new ObjectMapper().convertValue(stateMap.get("values"), Map.class);

                Map<String, Object>[] actionsMap = new ObjectMapper().convertValue(argsMap.get("actions"), Map[].class);
                String blockId = actionsMap[0].get("block_id").toString();

                Map<String, Object> blockIdMap = new ObjectMapper().convertValue(valuesMap.get(blockId), Map.class);
                Map<String, Object> staticSelectActionMap = new ObjectMapper().convertValue(blockIdMap.get("static_select-action"), Map.class);
                Map<String, Object> selectedOptionMap = new ObjectMapper().convertValue(staticSelectActionMap.get("selected_option"), Map.class);
                Map<String, Object> textMap = new ObjectMapper().convertValue(selectedOptionMap.get("text"), Map.class);

                // 完了する課題の課題Keyの取得
                String selectedValue = selectedOptionMap.get("value").toString();
                // 完了する課題のsummeryの取得
                String selectedText = textMap.get("text").toString();

                // 課題情報の更新(URL)
                String issuesUpdateUrl = "https://"+ domainName + "/api/v2/issues/"+selectedValue+"?apiKey="+ apiKey;

                // 状態を完了（statusId=4）をパラメータに設定する
                MultiValueMap<String,Integer> map = new LinkedMultiValueMap<>();
                map.add("statusId", 4);

                RequestEntity<?> request = RequestEntity.patch(URI.create(issuesUpdateUrl))
                                                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                        .accept(MediaType.APPLICATION_FORM_URLENCODED)
                                                        .body(map);


                RestTemplateBuilder builder = new RestTemplateBuilder();
                RestTemplate rest = builder.build();

                // request
                rest.exchange(request, NulabIssuesDto.class);

                // インスタンスの生成
                Slack slack = Slack.getInstance();

                slack.methods(token).chatPostMessage(req -> req
                .channel(channel)
                .text("「"+selectedText+"」の状態を完了に更新しました。")
                );

                return returnJson;
            default:
                throw new Exception();
        }
    }

    /**
     * パラメータをMAP形式に変換処理
     * @param data リクエストパラメータのデータ
     * @return Map<String, Object> リクエストボディ
     * @author　Karin Nunokawa
     */
    private Map<String, Object> stringToMap(String data) throws Exception {

        try {
            Type mapTokenType = new TypeToken<Map<String, Object>>(){}.getType();
            // パラメータのStringをMap形式に変換処理
            Map<String, Object> returnMap = gson.fromJson(data, mapTokenType);
            return returnMap;
        } catch (Exception e) {
            System.out.println("StringからMapに変換処理時にエラーが発生しました。");
            throw e;
        }
    }
    /**
     * プロジェクト一覧の取得処理
     * @return NulabProjectDto[] プロジェクト一覧
     * @author　Karin Nunokawa
     */
    private NulabProjectDto[] getProjects() throws Exception {

        try {
            // プロジェクト一覧の取得(URL)
            String projecsUrl = "https://"+ domainName + "/api/v2/projects?apiKey="+ apiKey+"&archived=false";

            // request
            NulabProjectDto[] returnProjects = rest.getForObject(projecsUrl, NulabProjectDto[].class);

            return returnProjects;
        } catch (Exception e) {
            System.out.println("プロジェクト一覧の取得処理時にエラーが発生しました。");
            throw e;
        }
    }
    /**
     * 課題一覧の取得処理
     * @param projects パラメータ使用のプロジェクトID
     * @return NulabIssueDto[] 課題一覧リスト
     *  @author　Karin Nunokawa
     */
    private NulabIssueDto[] getIssues(String projects) throws Exception {

        try {
            // 課題一覧の取得(URL)
            String issuesUrl = "https://"+ domainName + "/api/v2/issues?apiKey="+ apiKey + projects;

            // request
            NulabIssueDto[] returnIssues = rest.getForObject(issuesUrl, NulabIssueDto[].class);

            return returnIssues;
        } catch (Exception e) {
            System.out.println("課題一覧の取得処理時にエラーが発生しました。");
            throw e;
        }
    }
    /**
     * プロジェクトID取得処理
     * @param projectsResponse プロジェクト一覧データ
     * @return StringBuilder プロジェクトID
     * @author　Karin Nunokawa
     */
    private StringBuilder getProjectId(NulabProjectDto[] projectsResponse) throws Exception {

        try {

            // 現在アクティブなプロジェクトを全て格納
            StringBuilder sb = new StringBuilder();
            // 取得したプロジェクトのプロジェクトIDのみ取り出す
            for (NulabProjectDto project : projectsResponse) {
                sb.append("&projectId[]="+project.getId());
            }

            return sb;
        } catch (Exception e) {
            System.out.println("課題一覧の取得処理時にエラーが発生しました。");
            throw e;
        }
    }
    /**
     * 課題数の取得処理
     * @param projects パラメータ使用のプロジェクトID
     * @return NulabIssueCountDto 課題数
     * @author　Karin Nunokawa
     */
    private NulabIssueCountDto getIssueCount(String projects) throws Exception {

        try {
            // 課題数の取得(URL)
            String issueCountUrl = "https://"+ domainName + "/api/v2/issues/count?apiKey="+ apiKey + projects+"&statusId[]=1&statusId[]=2&statusId[]=3";

            // request
            NulabIssueCountDto returnIssueCount = rest.getForObject(issueCountUrl, NulabIssueCountDto.class);

            return returnIssueCount;
        } catch (Exception e) {
            System.out.println("課題数の取得処理時にエラーが発生しました。");
            throw e;
        }
    }
}
