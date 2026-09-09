# FameBook Supabase setup

1. Open the Supabase dashboard for project `foelkeqfmovtxxrhqxpc`.
2. Open **SQL Editor** and create a new query.
3. Paste and run `001_initial_schema.sql`.
4. In **Authentication > Providers**, enable **Email** and keep Google disabled for now.
5. Do not add the service-role key to the Android project.

The Android app reads the project URL and publishable key from `local.properties`.
That file is local-only and must not be committed.
