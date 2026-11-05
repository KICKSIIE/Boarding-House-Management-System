<!-- coderabbit-ai-generated:v1 -->

# Boarding House Management System

<!-- coderabbit-ai-generated:v1 -->

<!-- coderabbit-ai-generated-section:v1:ab0cc716bde9af1b0758085575b2a8679ea8d608d0ff5d7b3c9108f282068982 -->

A JavaFX desktop application with account registration, login, room details, recorded rent payments, and maintenance requests. Payments are records only; the app does not process payments.

<!-- /coderabbit-ai-generated:v1 -->

## Requirements

- JDK 17 or newer and Maven 3.8 or newer.
- MySQL 8 and a database user with access to `boarding_system`.

## Database setup

For a new database, run `mysql -u <admin-user> -p < db/schema.sql`, then grant an application user access to the `boarding_system` database. The schema defines unique email and username fields.

For an existing `users` table, first back up the database. Check for duplicate or NULL email and username values and resolve them. Inspect the existing columns and indexes, then adapt and run `db/upgrade-existing.sql` if needed. The migration widens the password column and adds uniqueness constraints; it must not be run against the new schema. Existing SHA-256 password hashes are upgraded after successful login. Their original platform character encoding must match the current Java default encoding for non-ASCII passwords.

<!-- coderabbit-ai-generated:v1 -->

<!-- coderabbit-ai-generated-section:v1:15dd654965f0c350022b19147bcace7f149201fe79b456f010d0c04dfcfaa91b -->

After creating or upgrading the account schema, run `mysql -u <admin-user> -p boarding_system < db/tenant-features.sql` to create the tenant tables. For an existing database, check that `users.email` has the same type, character set and collation as `tenant_rooms.user_email` in the script before applying it. Back up and adapt the schema if necessary. The script does not populate rooms or payments.

Room assignments and payment records are maintained in the database for now: add rows to `rooms`, `tenant_rooms`, and `rent_payments` for registered account email addresses. Staff can update `maintenance_requests.status` in the database. The tenant screen displays only the logged-in email's records and can submit maintenance requests. There is no manager screen or online payment processing.

The desktop application connects directly to the database using the configured database account. The app's screen filters are not a security boundary against someone with access to those credentials; use a server with per-user authorization before exposing this to untrusted users.

Set these environment variables before starting the application:

<!-- /coderabbit-ai-generated:v1 -->

- `BOARDING_DB_USER`: the database user.
- `BOARDING_DB_PASSWORD`: the database password (an empty value is allowed).
- `BOARDING_DB_URL`: optional JDBC URL; defaults to `jdbc:mysql://localhost:3306/boarding_system`.

## Run and test

<!-- coderabbit-ai-generated:v1 -->

<!-- coderabbit-ai-generated-section:v1:db49ac96591577b8645283a419ff19f74ed7eb8545311653fd039231c72b9282 -->

Run `mvn javafx:run` on a machine with a graphical desktop. Run `mvn test` for account and tenant tests, which use an in-memory database and do not require MySQL. The application needs the MySQL server and both schema scripts above for actual use.

<!-- /coderabbit-ai-generated:v1 -->
