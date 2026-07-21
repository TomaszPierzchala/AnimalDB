import { apiJson } from './apiClient';

const TRANSLINE_API_URL =
  `${import.meta.env.VITE_API_URL}/api/transgenic-lines`;

export function getTransLines() {
  return apiJson(TRANSLINE_API_URL);
}

export function createTransLine(transLine) {
  return apiJson(TRANSLINE_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(transLine)
  });
}

export function updateTransLine(id, transLine) {
  return apiJson(`${TRANSLINE_API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(transLine)
  });
}

export function deleteTransLine(id) {
  return apiJson(`${TRANSLINE_API_URL}/${id}`, {
    method: 'DELETE'
  });
}
