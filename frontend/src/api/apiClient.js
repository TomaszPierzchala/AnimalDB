
export async function apiJson(url, options = {}) {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      let errorMessage = `Server returned the error ${response.status}`;

      try {
        const errorBody = await response.json();

        if (
          errorBody.message &&
          errorBody.message.trim() !== ''
        ) {
          errorMessage = errorBody.message;
        }
      } catch {
        // Response body was empty or was not valid JSON.
      }

      throw new Error(errorMessage);
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