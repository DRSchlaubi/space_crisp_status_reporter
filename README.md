# Crisp Space Status Bridge
This posts [Crisp Status Updates](https://crisp.chat/en/status/) into a [JetBrains Space](https://jetbrains.space) channel like the Crisp Slack integration.

# Examples
![Example](https://rice.by.devs-from.asia/chrome_8De8yybigv.png)

# Setup
- Create a Crisp Webhook subscribing to the `status:health:changed` event to `<yourhost>:8080/crisp/receive=key=<key>`
- Create a [Space Application](https://www.jetbrains.com/help/space/applications.html)
- Run the [Docker container](https://hub.docker.com/r/schlaubiboy/space_crisp_status/tags?page=1&ordering=last_updated)
```shell
 docker run  --name crisp-status \
> -p 127.0.0.1:8745:8080 \
> -e CRISP_SECRET=<secret from step 1> \
> -e SPACE_URL=https://<org>.jetbrains.space/ \
> -e SPACE_CHANNEL_ID=<channelid> \
> -e SPACE_CLIENT_ID=<id from step 2>\
> -e SPACE_CLIENT_SECRET=<secret from step 2>\
> schlaubiboy/space_crisp_status:<tag from link above>
```

# Environment variables definition: [here](https://github.com/DRSchlaubi/space_crisp_status_reporter/tree/main/src/config/Config.kt#L6-32)