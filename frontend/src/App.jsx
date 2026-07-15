import GeneView from './views/genes/GeneView';
import GitHubLink from './components/GitHubLink';
import './App.css';

function App() {
  return (
    <>
      <header className="app-header">
        <div className="app-logo">
          AnimalDB
        </div>
      </header>

      <main>
        <GeneView />
      </main>

      <GitHubLink />
    </>
  );
}

export default App;
