
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

      if(response.status === 501){
        errorMessage = "Server returned error 501 - Not Implemented yet"
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
          'Cannot fetch data from backend.\n' +
          'The server may be unavailable, or the request may be blocked by CORS.',
          { cause: err }
	);
    }

    throw err;
  }
}