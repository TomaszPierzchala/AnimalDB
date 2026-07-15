function ErrorBanner({ message, fading }) {
  if (!message) {
    return null;
  }

  return (
    <p className={fading ? 'error error-fading' : 'error'}>
      {message}
    </p>
  );
}

export default ErrorBanner;
