import { useState } from 'react';

import TwoColumnView from './views/TwoColumnView';
import StrainView from './views/strains/StrainView';

import GitHubLink from './components/GitHubLink';
import Sidebar from './components/Sidebar';

import {
  VIEW_GENE,
  VIEW_STRAIN
} from './viewNames';

import {
  createGene,
  deleteGene,
  getGenes,
  updateGene
} from './api/geneApi';

import './App.css';

function App() {
  const [activeView, setActiveView] = useState(VIEW_GENE);

  function renderActiveView() {
    switch (activeView) {
      case VIEW_GENE:
        return renderGeneView();

      case VIEW_STRAIN:
        return <StrainView />;

      default:
        return renderGeneView();
    }
  }

  function renderGeneView() {
    return (
      <TwoColumnView
        entityName='Gene'
        firstName='symbol'
        secondName='description'
        createApi={createGene}
        getApi={getGenes}
        updateApi={updateGene}
        deleteApi={deleteGene}
      />
    );
  }

  return (
    <div className="app-layout">
      <aside className="app-sidebar">
        <div className="app-logo">
          AnimalDB
        </div>

        <Sidebar
          activeView={activeView}
          onViewChange={setActiveView}
        />
		
		<GitHubLink />
      </aside>

      <main className="app-content">
        {renderActiveView()}
      </main>
      
    </div>
  );
}

export default App;