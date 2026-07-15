import { useEffect, useState } from 'react';

import {
  createGene,
  deleteGene,
  getGenes,
  updateGene
} from '../../api/geneApi';

import ErrorBanner from '../../components/ErrorBanner';
import GeneForm from './GeneForm';
import { validateSymbol } from './geneValidation';
import GeneTable from './GeneTable';

import './GeneView.css';

function GeneView() {
  const [genes, setGenes] = useState([]);

  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
  const [refreshAfterError, setRefreshAfterError] =
    useState(false);

  const [popupOpen, setPopupOpen] = useState(false);
  const [editingGene, setEditingGene] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [symbol, setSymbol] = useState('');
  const [description, setDescription] = useState('');
  const [symbolWarning, setSymbolWarning] = useState('');

  useEffect(() => {
    loadGenes();
  }, []);

  useEffect(() => {
    if (!error) {
      return;
    }

    const fadeTimer = setTimeout(() => {
      setErrorFading(true);
    }, 3000);

    const clearTimer = setTimeout(async () => {
      setError('');
      setErrorFading(false);

      if (refreshAfterError) {
        setRefreshAfterError(false);
        await loadGenes();
      }
    }, 6500);

    return () => {
      clearTimeout(fadeTimer);
      clearTimeout(clearTimer);
    };
  }, [error, refreshAfterError]);

  async function loadGenes() {
    try {
      const data = await getGenes();

      setGenes(data);
      setError('');
    } catch (err) {
      showError(
        'Could not load genes.\n' + err.message
      );
    }
  }

  function showError(message) {
    setErrorFading(false);
    setError(message);
  }

  function openCreatePopup() {
    setEditingGene(null);
    setSymbol('');
    setDescription('');
    setDeleteArmed(false);
    setSymbolWarning(validateSymbol(''));
    setPopupOpen(true);
  }

  function openEditPopup(gene) {
    setEditingGene(gene);
    setSymbol(gene.symbol);
    setDescription(gene.description ?? '');
    setDeleteArmed(false);
    setSymbolWarning(validateSymbol(gene.symbol));
    setPopupOpen(true);
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

    const validationMessage = validateSymbol(symbol);

    if (symbol.trim() === '') {
      setSymbolWarning(validationMessage);
      return;
    }

    if (editingGene !== null && !hasChanges) {
      return;
    }

    const requestBody = {
      symbol,
      description
    };

    try {
      if (editingGene === null) {
        await createGene(requestBody);
      } else {
        await updateGene(editingGene.id, requestBody);
      }

      await refreshAfterPopup();
    } catch (err) {
      showError(
        (
          editingGene === null
            ? 'Could not create a new gene.'
            : 'Could not update the gene.'
        ) +
        '\n' +
        err.message
      );

      if (editingGene !== null) {
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
      await deleteGene(editingGene.id);
      await refreshAfterPopup();
    } catch (err) {
      showError(
        'Could not delete the gene.\n' +
        err.message
      );

      closePopup();
      setRefreshAfterError(true);
    }
  }

  const hasChanges =
    editingGene === null ||
    symbol !== editingGene.symbol ||
    description !==
      (editingGene.description ?? '');

  return (
    <section>
      <ErrorBanner
        message={error}
        fading={errorFading}
      />

      <h1>Genes</h1>

      <GeneTable
        genes={genes}
        onEdit={openEditPopup}
        onCreate={openCreatePopup}
      />

      {popupOpen && (
        <GeneForm
          editingGene={editingGene}
          symbol={symbol}
          description={description}
          symbolWarning={symbolWarning}
          deleteArmed={deleteArmed}
          hasChanges={hasChanges}
          onSymbolChange={(value, warning) => {
            setSymbol(value);
            setSymbolWarning(warning);
          }}
          onDescriptionChange={setDescription}
          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={closePopup}
        />
      )}
    </section>
  );
}

export default GeneView;
