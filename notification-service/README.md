# Notification service

## Prerequisites

### Mongodb
Install Mongodb from Docker Hub

`docker pull bitnami/mongodb:7.0.11`

Start Mongodb server at port 27017 with root username and password: root/root

`docker run -d --name mongodb-7.0.11 -p 27017:27017 -e MONGODB_ROOT_USER=root -e MONGODB_ROOT_PASSWORD=root bitnami/mongodb:7.0.11`

### Environment Variables

The following environment variables are required to run the notification service:

- `BREVO_KEY` (required): Your Brevo API key for sending emails. You can obtain this from your [Brevo account](https://app.brevo.com/).
- `BREVO_URL` (optional): The Brevo API URL. Defaults to `https://api.brevo.com` if not specified.

You can set these variables in your environment or create a `.env` file in the notification-service directory:

```bash
BREVO_KEY=your-brevo-api-key-here
BREVO_URL=https://api.brevo.com
```

**Note:** The `.env` file is excluded from version control for security reasons. Never commit API keys to the repository.
