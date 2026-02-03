#!/usr/bin/env node
const crypto = require("crypto");
const fs = require("fs");
const path = require("path");

const rootDir = path.resolve(__dirname, "..");
const privateKeyPath =
  process.env.DEV_PRIVATE_KEY_PATH || path.join(rootDir, "dev-private.pem");

const requiredEnv = [
  "DEV_TOKEN_SUB",
  "DEV_TOKEN_USERNAME",
  "DEV_TOKEN_EMAIL",
  "DEV_TOKEN_ROLES",
  "DEV_TOKEN_SCOPE",
  "DEV_TOKEN_AUDIENCE",
  "DEV_TOKEN_ISSUER",
];

const missing = requiredEnv.filter((key) => !process.env[key]);
if (missing.length > 0) {
  console.error(`Missing required env vars: ${missing.join(", ")}`);
  process.exit(1);
}

const now = Math.floor(Date.now() / 1000);
const ttlDays = Number.parseInt(process.env.DEV_TOKEN_TTL_DAYS || "3650", 10);
const exp = now + ttlDays * 24 * 60 * 60;

const roles = process.env.DEV_TOKEN_ROLES.split(",")
  .map((role) => role.trim())
  .filter(Boolean);

const payload = {
  sub: process.env.DEV_TOKEN_SUB,
  preferred_username: process.env.DEV_TOKEN_USERNAME,
  email: process.env.DEV_TOKEN_EMAIL,
  realm_access: {
    roles,
  },
  scope: process.env.DEV_TOKEN_SCOPE,
  aud: [process.env.DEV_TOKEN_AUDIENCE],
  iss: process.env.DEV_TOKEN_ISSUER,
  iat: now,
  exp,
};

const header = {
  alg: "RS256",
  typ: "JWT",
};

const input = [
  base64Url(JSON.stringify(header)),
  base64Url(JSON.stringify(payload)),
].join(".");

const privateKey = fs.readFileSync(privateKeyPath);
const signature = crypto.sign("RSA-SHA256", Buffer.from(input), privateKey);
const token = `${input}.${base64Url(signature)}`;

process.stdout.write(`${token}\n`);

function base64Url(value) {
  const buffer = Buffer.isBuffer(value) ? value : Buffer.from(value);
  return buffer
    .toString("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
}
