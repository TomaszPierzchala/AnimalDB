import { useState } from 'react';

import TwoColumnView from './views/TwoColumnView';

import GitHubLink from './components/GitHubLink';
import Sidebar from './components/Sidebar';
import TransgenicLineView from './views/TransgenicLineView';
import TransgenicLineGenesView from './views/TransgenicLineGenesView';
import { FIRST, SECOND } from './utils/const';

import {
  VIEW_GENE,
  VIEW_STRAIN,
  VIEW_TRANSLINE,
  VIEW_TRANSLINEGENES
} from './viewNames';

import {
  createGene,
  deleteGene,
  getGenes,
  updateGene
} from './api/geneApi';
import {
  createStrain,
  deleteStrain,
  getStrains,
  updateStrain
} from './api/strainApi';

import './App.css';

function App() {
  const [activeView, setActiveView] = useState(() => {
    const savedView = localStorage.getItem('activeView');

    if (
      savedView === VIEW_GENE ||
      savedView === VIEW_STRAIN ||
      savedView === VIEW_TRANSLINE ||
      savedView === VIEW_TRANSLINEGENES
    ) {
      return savedView;
    }

    return VIEW_GENE;
  });

  function changeView(view) {
    setActiveView(view);
    localStorage.setItem('activeView', view);
  }

  function renderActiveView() {
    switch (activeView) {
      case VIEW_GENE:
        return renderGeneView();

      case VIEW_STRAIN:
        return renderStrainView();

      case VIEW_TRANSLINE:
        return renderTransgenicLineView();

      case VIEW_TRANSLINEGENES:
        return renderTransgenicLineGenesView();

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
        warningKey={FIRST}
        createApi={createGene}
        getApi={getGenes}
        updateApi={updateGene}
        deleteApi={deleteGene}
      />
    );
  }

  function renderStrainView() {
    return (
      <TwoColumnView
        entityName='Strain'
        firstName='code'
        secondName='name'
        warningKey={FIRST | SECOND}
        createApi={createStrain}
        getApi={getStrains}
        updateApi={updateStrain}
        deleteApi={deleteStrain}
      />
    );
  }
 
  function renderTransgenicLineView() {
    return <TransgenicLineView />;
  }

  function renderTransgenicLineGenesView() {
	/*return <TransgenicLineGenesView />; */
	return <TransgenicLineGenesView/>;
  }

  return (
    <div className="app-layout">
      <aside className="app-sidebar">
        <div className="app-logo">
          AnimalDB
        </div>

        <Sidebar
          activeView={activeView}
          onViewChange={changeView}
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