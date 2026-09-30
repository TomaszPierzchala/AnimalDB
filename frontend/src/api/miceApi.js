import {apiJson} from './apiClient';

const MOUSE_API_URL =
    `${import.meta.env.VITE_API_URL}/api/mice`;

export function getMice({
                          page = 0,
                          size = 20,
                          sortBy = 'animalNumber',
                          direction = 'asc'
                        } = {}) {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sort: `${sortBy},${direction}`
  });

  return apiJson(`${MOUSE_API_URL}?${params}`);
}

export function createMouse(mouse) {
    return apiJson(MOUSE_API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(mouse)
    });
}

export function updateMouse(id, mouse) {
    return apiJson(`${MOUSE_API_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(mouse)
    });
}

export function deleteMouse(id) {
    return apiJson(`${MOUSE_API_URL}/${id}`, {
        method: 'DELETE'
    });
}
