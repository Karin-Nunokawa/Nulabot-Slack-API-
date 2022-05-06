# Nulabot

 You can update backlog issues from slack.

# DEMO

 ![demo](https://user-images.githubusercontent.com/101876093/167077583-197f86be-1951-4c4a-9db6-311f329601e9.gif)

# Usage

 ▼If you want just use it.

 1.If you want to use it, please let me know as I will invite you to the workspace in Slack.

 2.Please refer to the Executable command (text) and send the message to the # nulab-exam channel.

 ▼If you want to run in your local environment.

 1.Set the following values ​​in the system environment variables.
   "BACKLOG_DOMAIN_NAME": Your backlog domain name.
   "BACKLOG_API_KEY": Your backlog API key.
   "SLACK_BOT_TOKEN": Ask me.

 2.Install ngrok.

 3.Start the server in the local environment and execute the following command.
   `ngrok http 8080`

 4."Forwarding" displayed on the command prompt is your URL that has been published externally.
   Please tell me that.
   I register it from the Slack API admin page and configure it to work in your local environment.

 5.If you want to use it, please let me know as I will invite you to the workspace in Slack.

 6.Please refer to the Executable command (text) and send the message to the # nulab-exam channel.

# Executable command (text)

 "課題一覧の取得"：You can get a list of issues for currently active projects.

 "課題数の取得"：You can get the number of issues whose status is not "Completed".

 "課題の完了"：You can select an issue from the list of issues and update the status to "Complete".

# Author

 Karin Nunokawa
