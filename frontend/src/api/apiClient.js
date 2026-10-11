
export async function apiJson(url, options = {}) {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      const contentType = response.headers.get('content-type');

      let errorMessage = `Server returned the error ${response.status}.`;

      try {
        if (contentType?.includes('application/json')) {
          const errorBody = await response.json();

          errorMessage +=
              '\n' + (errorBody.message ?? JSON.stringify(errorBody));
        } else {
          const text = (await response.text()).trim();

          if (text) {
            errorMessage += '\n' + text;
          }
        }
      } catch {
        // empty or improper body
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