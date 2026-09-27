import axios from "axios";
import { normalizeApiResponse } from "../utils/apiResponse";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

const PUBLIC_AUTH_ENDPOINTS = [
  "/auth/login",
  "/auth/register",
  "/auth/verify-otp",
  "/auth/resend-otp",
  "/auth/forgot-password",
  "/auth/reset-password",
];

api.interceptors.request.use((config) => {
  const isPublicAuthEndpoint = PUBLIC_AUTH_ENDPOINTS.some((path) =>
    config.url?.includes(path)
  );
  const token = localStorage.getItem("token");
  if (token && !isPublicAuthEndpoint) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => normalizeApiResponse(response),
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default api;
