import { apiJson } from './apiClient';

const TRANSLINEGENE_API_URL =
  `${import.meta.env.VITE_API_URL}/api/transgenic-line-genes`;

export function getTransLineGenes() {
  return apiJson(TRANSLINEGENE_API_URL);
}

export function createTransLineGene(data) {
  return apiJson(TRANSLINEGENE_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
}

export function updateTransLineGene(
  currentTransgenicLineId,
  currentGeneId,
  data
 ) {
  return apiJson(`${TRANSLINEGENE_API_URL}/${currentTransgenicLineId}/${currentGeneId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
}

export function deleteTransLineGene(
  transgenicLineId,
  geneId
 ) {
  return apiJson(`${TRANSLINEGENE_API_URL}/${transgenicLineId}/${geneId}`, {
    method: 'DELETE'
  });
}

export function deleteTransLineGeneByTransgenicLine(
  transgenicLineId
 ) {
  return apiJson(`${TRANSLINEGENE_API_URL}/${transgenicLineId}`, {
    method: 'DELETE'
  });
}
