import { apiJson } from './apiClient';

const STRAIN_API_URL =
  `${import.meta.env.VITE_API_URL}/api/strain`;

export function getStrains() {
  return apiJson(STRAIN_API_URL);
}

export function createStrain(strain) {
  return apiJson(STRAIN_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(strain)
  });
}

export function updateStrain(id, strain) {
  return apiJson(`${STRAIN_API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(strain)
  });
}

export function deleteStrain(id) {
  return apiJson(`${STRAIN_API_URL}/${id}`, {
    method: 'DELETE'
  });
}
