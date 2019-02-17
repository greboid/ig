CREATE TABLE Accounts (
    ID int NOT NULL AUTO_INCREMENT,
    username varchar(50) NOT NULL,
    password varchar(255) NOT NULL,
    isAdmin BOOLEAN NOT NULL,
    UNIQUE INDEX username (username),
    CONSTRAINT Accounts_pk PRIMARY KEY (ID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE Categories (
    ID int NOT NULL AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    Account_ID int NOT NULL,
    CONSTRAINT id PRIMARY KEY (ID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE CategoryMap (
    ID int NOT NULL AUTO_INCREMENT,
    Categories_ID int NOT NULL,
    Source_ID int NOT NULL,
    CONSTRAINT CategoryMap_pk PRIMARY KEY (ID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE Sources (
    ID int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    lastpoll datetime,
    SourceType_ID int NOT NULL,
    UNIQUE INDEX uniqueNameForSourceType (ID,SourceType_ID),
    CONSTRAINT Source_pk PRIMARY KEY (ID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE SourceItems (
    ID int NOT NULL AUTO_INCREMENT,
    identifier varchar(100) not null,
    `index` INT NOT NULL DEFAULT 0,
    thumbnail text NOT NULL,
    url text NOT NULL,
    caption text NOT NULL,
    `timestamp` BIGINT NOT NULL,
    Source_ID int NOT NULL,
    CONSTRAINT SourceItems_pk PRIMARY KEY (ID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE SourceTypes (
    ID int NOT NULL AUTO_INCREMENT,
    name varchar(100) NOT NULL,
    CONSTRAINT SourceTypes_pk PRIMARY KEY (ID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE VIEW SourceItemsFromCategories AS
SELECT
SourceTypes.name as SourceTypeName,
SourceItems.caption as Caption,
Sources.name as SourceName,
Categories.name as CategoryName,
Accounts.username as AccountUsername
FROM SourceItems
LEFT JOIN Sources on Sources.ID=SourceItems.Source_ID
LEFT JOIN SourceTypes on SourceTypes.ID=Sources.SourceType_ID
LEFT JOIN CategoryMap on CategoryMap.Source_ID=Sources.ID
LEFT JOIN Categories on CategoryMap.Source_ID=Categories.Account_ID
LEFT JOIN Accounts on Accounts.ID=Categories.Account_ID;
ALTER TABLE Categories ADD CONSTRAINT Account_Categories FOREIGN KEY Account_Categories (Account_ID)
    REFERENCES Accounts (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE;
ALTER TABLE CategoryMap ADD CONSTRAINT CategoryMap_Categories FOREIGN KEY CategoryMap_Categories (Categories_ID)
    REFERENCES Categories (ID)
    ON DELETE CASCADE;
ALTER TABLE CategoryMap ADD CONSTRAINT CategoryMap_Source FOREIGN KEY CategoryMap_Source (Source_ID)
    REFERENCES Sources (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE;
ALTER TABLE SourceItems ADD CONSTRAINT SourceItems_Source FOREIGN KEY SourceItems_Source (Source_ID)
    REFERENCES Sources (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE;
ALTER TABLE Sources ADD CONSTRAINT Sources_SourceType FOREIGN KEY Sources_SourceType (SourceType_ID)
    REFERENCES SourceTypes (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE;