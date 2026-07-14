import { useEffect, useState } from 'react';
import { apiJson } from './api/apiClient';
import './App.css';

const API_URL = `${import.meta.env.VITE_API_URL}/api/genes`;
const SYMBOL_MAX_LENGTH = 50;

function App() {
  const [genes, setGenes] = useState([]);
  const [symbolWarning, setSymbolWarning] = useState('');
  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
  const [refreshAfterError, setRefreshAfterError] = useState(false);

  const [popupOpen, setPopupOpen] = useState(false);
  const [editingGene, setEditingGene] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [symbol, setSymbol] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    loadGenes();
  }, []);

  useEffect(() => {
    if (!refreshAfterError || error !== '') {
      return;
    }

    async function refresh() {
      setRefreshAfterError(false);
      await loadGenes();
    }

    refresh();
  }, [error, refreshAfterError]);

  useEffect(() => {
    if (!error) {
      return;
    }

    const fadeTimer = setTimeout(() => {
      setErrorFading(true);
    }, 3000);

    const clearTimer = setTimeout(() => {
      setError('');
      setErrorFading(false);
    }, 6500);

    return () => {
      clearTimeout(fadeTimer);
      clearTimeout(clearTimer);
    };
  }, [error]);

  async function loadGenes() {
    try {
      const data = await apiJson(API_URL);

      setGenes(data);
      setError('');
    } catch (err) {
      showError('Could not load genes.\n' + err.message);
    }
  }
  
  function showError(message) {
    setErrorFading(false);
    setError(message);
  }

  function validateSymbol(value) {
    if (value.trim() === '') {
      return 'Gene symbol is required.';
    }

    if (value.length === SYMBOL_MAX_LENGTH) {
      return `Maximum length (${SYMBOL_MAX_LENGTH}) reached.`;
    }

    return '';
  }

  function openCreatePopup() {
    setEditingGene(null);
    setSymbol('');
    setDescription('');
    setPopupOpen(true);
	setDeleteArmed(false);
	setSymbolWarning(validateSymbol(''));
  }

  function openEditPopup(gene) {
    setEditingGene(gene);
    setSymbol(gene.symbol);
    setDescription(gene.description ?? '');
    setPopupOpen(true);
	setDeleteArmed(false);
	setSymbolWarning(validateSymbol(gene.symbol));
  }

  function closePopup() {
    setPopupOpen(false);
    setEditingGene(null);
    setSymbol('');
    setDescription('');
	setDeleteArmed(false);
	setSymbolWarning('');
  }
  
  async function refreshAfterPopup() {
    closePopup();
    await loadGenes();
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (symbol.trim() === '') {
      setSymbolWarning('Gene symbol is required.');
      return;
    }
    const requestBody = {
      symbol: symbol,
      description: description
    };

    if (editingGene !== null && !hasChanges) {
      return;
    }

    try {
      if (editingGene === null) {
        await apiJson(API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestBody)
        });
      } else {
        await apiJson(`${API_URL}/${editingGene.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestBody)
        });
      }

      await refreshAfterPopup();
    } catch (err) {
      showError((editingGene === null
                                ? 'Could not create a new gene. '
                                : 'Could not update the gene. ')
                                   + '\n' + err.message);
	 if(editingGene !== null) {
		closePopup();
		setRefreshAfterError(true);
	 }
    }
  }
  async function handleDelete() {
    if (editingGene === null) {
      return;
    }

	if (!deleteArmed) {
	    setDeleteArmed(true);
	    return;
	}

    try {
      await apiJson(`${API_URL}/${editingGene.id}`, {
        method: 'DELETE'
      });

      await refreshAfterPopup();
    } catch (err) {
      showError('Could not delete the gene.\n' + err.message);
      closePopup();
    }
  }

  const hasChanges =
    editingGene === null ||
    symbol !== editingGene.symbol ||
    description !== (editingGene.description ?? '');

  return (
    <>
      <div className="app-logo">
        AnimalDB
      </div>
	  <a
	    className="github-project-link"
	    href="https://github.com/TomaszPierzchala/AnimalDB"
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

      <main>
        {error && (
          <p className={errorFading ? 'error error-fading' : 'error'}>
            {error}
          </p>
        )}

        <section>
          <h1>Genes</h1>

          <table>
            <colgroup>
              <col className="id-table-column" />
              <col className="symbol-table-column" />
              <col className="description-table-column" />
            </colgroup>
            <thead>
              <tr>
                  <th className="id-column">ID</th>
                  <th className="symbol-column">Symbol</th>
                  <th className="description-column">Description</th>
              </tr>
            </thead>

            <tbody>
              {genes.map((gene) => (
                <tr
                  key={gene.id}
                  className="clickable-row"
                  onClick={() => openEditPopup(gene)}
                >
					<td className="id-column">{gene.id}</td>
					<td>{gene.symbol}</td>
					<td>{gene.description}</td>
                </tr>
              ))}

			  <tr
			    className="empty-row"
			    onClick={openCreatePopup}
			  >
			    <td className="id-column add-icon-cell">
			      <span className="add-icon">+</span>
			    </td>

			    <td colSpan="2" className="add-text-cell">
			      Click here to add a new gene...
			    </td>
			  </tr>
            </tbody>
          </table>
        </section>

        {popupOpen && (
          <div className="popup-backdrop">
            <div className="popup">
              <h2>
                {editingGene === null ? 'Add gene' : 'Edit gene'}
              </h2>

              <form onSubmit={handleSubmit}>
                <div>
                  <label>
                    Symbol:
                    <input
                      type="text"
                      value={symbol}
                      onChange={(event) => {
                        const value = event.target.value;
                        setSymbol(value);

                        setSymbolWarning(validateSymbol(value));
                      }}
                      maxLength={SYMBOL_MAX_LENGTH}
                    />
                    <small className="field-warning">
                      {symbolWarning}
                    </small>
                  </label>
                </div>

                <div>
                  <label>
                    Description:
                    <input
                      type="text"
                      value={description}
                      onChange={(event) => setDescription(event.target.value)}
                    />
                  </label>
                </div>

                <div className="popup-buttons">
                  <div>
                    {editingGene !== null && (
                      <button
                        type="button"
                        className={deleteArmed ? 'delete-button delete-armed' : 'delete-button'}
                        onClick={handleDelete}
                      >
                        {deleteArmed ? 'Confirm delete' : 'Delete'}
                      </button>
                    )}
                  </div>

                  <div className="popup-main-buttons">
                    <button type="submit"
                            disabled={editingGene !== null && !hasChanges}
                    >
                      {editingGene === null ? 'Add' : 'Save'}
                    </button>

				    <button type="button" onClick={closePopup}>
                      Cancel
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </>
  );
}

export default App;