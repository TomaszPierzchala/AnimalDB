import {apiJson} from './apiClient';

const MATING_API_URL =
    `${import.meta.env.VITE_API_URL}/api/mating`;


export function getMatings({
                          page = 0,
                          size = 20,
                          sortBy = 'femaleAnimalNumber',
                          direction = 'asc'
                        } = {}) {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sort: `${sortBy},${direction}`
  });

  return apiJson(`${MATING_API_URL}?${params}`);
}

export function createMating(mouse) {
    return apiJson(MATING_API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(mouse)
    });
}

export function updateMating(id, mouse) {
    return apiJson(`${MATING_API_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(mouse)
    });
}

export function deleteMating(id) {
    return apiJson(`${MATING_API_URL}/${id}`, {
        method: 'DELETE'
    });
}
