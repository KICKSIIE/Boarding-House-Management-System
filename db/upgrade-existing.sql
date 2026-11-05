-- Back up the database first. Resolve duplicate emails/usernames and NULL values
-- before running this migration. Do not run it on a newly created schema.
ALTER TABLE users
    MODIFY email VARCHAR(254) NOT NULL,
    MODIFY username VARCHAR(100) NOT NULL,
    MODIFY password VARCHAR(255) NOT NULL,
    ADD CONSTRAINT users_email_unique UNIQUE (email),
    ADD CONSTRAINT users_username_unique UNIQUE (username);
