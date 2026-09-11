export const API_BASE_URL = 'https://localhost:8443';
export const GOOGLE_LOGIN_URL = `${API_BASE_URL}/oauth2/authorization/google`;
// TikTok no se fija aquí porque el callback puede ser un host público (ngrok).
// El frontend lo obtiene de /api/v1/auth/social/tiktok/start-url.
