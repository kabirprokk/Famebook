# FameBook Supabase setup

1. Open the Supabase dashboard for project `foelkeqfmovtxxrhqxpc`.
2. Open **SQL Editor** and create a new query.
3. Paste and run `001_initial_schema.sql`.
4. Paste and run `002_admin_role_management.sql` to enable admin-only in-app role assignment.
5. Paste and run `003_crew_availability_fix.sql` so crew can create their availability profile.
6. Paste and run `004_notification_fanout.sql` so booking requests, acceptances, and cancellations notify the other party's device.
5. In **Authentication > Providers**, enable **Email** and keep Google disabled for now.
5. In **Authentication > URL Configuration > Redirect URLs**, add:
   `famebook://auth/callback`
6. In **Authentication > Email Templates > Confirm signup**, paste `email-confirmation.html`.
7. Do not add the service-role key to the Android project.

The Android app reads the project URL and publishable key from `local.properties`.
That file is local-only and must not be committed.
