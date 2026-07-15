const REPOSITORY_URL =
  'https://github.com/TomaszPierzchala/AnimalDB';

function GitHubLink() {
  return (
    <a
      className="github-project-link"
      href={REPOSITORY_URL}
      target="_blank"
      rel="noopener noreferrer"
      title="AnimalDB repository on GitHub"
      aria-label="Open AnimalDB repository on GitHub"
    >
      <img
        src="/GitHub_Invertocat_Black.svg"
        alt=""
      />
    </a>
  );
}

export default GitHubLink;
