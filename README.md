#### Quick intro
Thanks for your interest in the project - it is mostly for personal use
but if anyone else finds it useful, that's great.

If you're looking to contribute, [see here](.github/CONTRIBUTING.md)

If you find a bug, please [raise an issue](https://github.com/greboid/ig/issues/new)
or [pull request](https://help.github.com/articles/creating-a-pull-request/) and I'll take a look.

#### Instructions
By default this is intended to be run with docker, there's a compose file
available and this should set everything up.

 - git clone
 - docker-compose up -d
 - This creates a default config file for you, config.yml in a docker volume named config
 - The admin details will be output to docker logs
 
If you're doing this yourself, you will need the following:
 - JVM (Tested on 12)
 - Gradle (Tested on 5)
 - MySQL (Tested 8)

#### Config Options

Setting|Default|Comments
---|---|---
dbhost|ig|Defaults to database, unless using included docker-compose.
db|ig|Must be created first, unless using included docker-compose.
dbuser|ig|
dbpassword|ig|
dbport|3306|
sessionKey|[Random String]|Value used to encrypt sessions.
adminUsername|admin|Default admin username
adminPassword|[Random String]|Default admin password
webPort|80|Default port to listen on for web connections
refreshDelay|15|Total delay between polls
igLogin|false|Should we log in to poll
igUsername||Login username
igPassword||Login password
