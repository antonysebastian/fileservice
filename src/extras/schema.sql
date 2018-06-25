CREATE TABLE `files` (
  `path` varchar(256) NOT NULL COMMENT 'PK.It is the storage location of the files',
  `file_name` varchar(256) NOT NULL COMMENT 'PK. It is the name of the file',
  `encryption_key` varchar(128) NOT NULL COMMENT 'It is the key used for encryption of the file',
  PRIMARY KEY (`path`,`file_name`)
) COMMENT='Table to store metadata about files';

CREATE TABLE `users` (
  `login` varchar(64) NOT NULL COMMENT 'PK.It is the login of the user',
  `password` varchar(256) NOT NULL COMMENT 'It is the encrypted password of this user',
  `role` ENUM('admin', 'user') NOT NULL DEFAULT 'user' COMMENT 'Role of the user',
  PRIMARY KEY (`login`)
) COMMENT='Table to user details';
