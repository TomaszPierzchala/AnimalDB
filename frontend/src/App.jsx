import { useEffect, useState } from 'react';
import './App.css';

const API_URL = 'http://localhost:8080/api/genes';

function App() {
  const [genes, setGenes] = useState([]);
  const [error, setError] = useState('');

  const [popupOpen, setPopupOpen] = useState(false);
  const [editingGene, setEditingGene] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [symbol, setSymbol] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    loadGenes();
  }, []);

  async function loadGenes() {
    try {
      const response = await fetch(API_URL);

      if (!response.ok) {
        throw new Error('Could not load genes');
      }

      const data = await response.json();
      setGenes(data);
      setError('');
    } catch (err) {
      setError(err.message);
    }
  }

  function openCreatePopup() {
    setEditingGene(null);
    setSymbol('');
    setDescription('');
    setPopupOpen(true);
	setDeleteArmed(false);
  }

  function openEditPopup(gene) {
    setEditingGene(gene);
    setSymbol(gene.symbol);
    setDescription(gene.description ?? '');
    setPopupOpen(true);
	setDeleteArmed(false);
  }

  function closePopup() {
    setPopupOpen(false);
    setEditingGene(null);
    setSymbol('');
    setDescription('');
	setDeleteArmed(false);
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const requestBody = {
      symbol: symbol,
      description: description
    };

    if (editingGene !== null && !hasChanges) {
      return;
    }

    try {
      let response;

      if (editingGene === null) {
        response = await fetch(API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestBody)
        });
      } else {
        response = await fetch(`${API_URL}/${editingGene.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestBody)
        });
      }

      if (!response.ok) {
        throw new Error(
          editingGene === null
            ? 'Could not create gene'
            : 'Could not update gene'
        );
      }

      closePopup();
      await loadGenes();
    } catch (err) {
      setError(err.message);
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
      const response = await fetch(`${API_URL}/${editingGene.id}`, {
        method: 'DELETE'
      });

      if (!response.ok) {
        throw new Error('Could not delete gene');
      }

      closePopup();
      await loadGenes();
    } catch (err) {
      setError(err.message);
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

      <main>
        {error && (
          <p className="error">
            {error}
          </p>
        )}

        <section>
          <h1>Genes</h1>

          <table>
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
			    <td className="id-column"/>
			    <td className="add-icon-cell">
			       <span className="add-icon">+</span>
			    </td>
			    <td className="add-text-cell">
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
                      onChange={(event) => setSymbol(event.target.value)}
                      onInvalid={(event) =>
                        event.target.setCustomValidity('Gene symbol is required')
                      }
                      onInput={(event) =>
                        event.target.setCustomValidity('')
                      }
                      onMouseEnter={(event) => {
                          if (event.target.value.trim() === '') {
                            event.target.setCustomValidity('Gene symbol is required');
                          }
                      }}
                      onMouseLeave={(event) =>
                          event.target.setCustomValidity('')
                      }
                      required
                      maxLength={50}
                    />
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