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
 - This creates a default config file for you
 - docker-compose up -d

#### Config Options

Setting|Default|Description
---|---|---
database|jdbc:mysql://ig:ig@database/ig|Sets the JDBC url for the database, supports MySQL and SQLite<br><br>If you're not using docker, update the username and password as needed<br><br>To use SQLite, for dev for instance, use jdbc:sqlite:database/database.sqlite
sessionKey|9e424e10e3dcd2f4fdd8d811c54aa36c|**Change this**.  Value used to encrypt sessions.
adminUsername|admin|**Change this**.  Default admin password
adminPassword|admin|**Change this**.  Default admin username
webPort|80|Default port to listen on for web connections