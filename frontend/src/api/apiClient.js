
export async function apiJson(url, options = {}) {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      throw new Error(`Server returned the error ${response.status}`);
    }

    if (response.status === 204) {
      return null;
    }

    return await response.json();
  } catch (err) {
    if (err instanceof TypeError) {
      throw new Error(
		'Cannot connect to backend server. Please start the Spring Boot application.',
			{ cause: err }
	);
    }

    throw err;
  }
}