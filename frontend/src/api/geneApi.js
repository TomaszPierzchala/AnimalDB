import { apiJson } from './apiClient';

const GENE_API_URL =
  `${import.meta.env.VITE_API_URL}/api/genes`;

export function getGenes() {
  return apiJson(GENE_API_URL);
}

export function createGene(gene) {
  return apiJson(GENE_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(gene)
  });
}

export function updateGene(id, gene) {
  return apiJson(`${GENE_API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(gene)
  });
}

export function deleteGene(id) {
  return apiJson(`${GENE_API_URL}/${id}`, {
    method: 'DELETE'
  });
}
