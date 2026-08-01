import { useEffect, useState } from 'react';

import ErrorBanner from '../components/ErrorBanner';

import { getGenes } from '../api/geneApi';
import { getTransLines } from '../api/transgenicLineApi';
import AddNewRecordRow from './AddNewRecordRow'
import DoubleParamForm from './DoubleParamForm'
import { createFieldWarning, isCurrentInitialPopupStateChosen } from '../warnings/popupWarning'
import { ERROR_VISIBLE_TIME, ERROR_FADE_TIME } from '../utils/const'

import {
  createTransLineGene,
  deleteTransLineGene,
  deleteTransLineGeneByTransgenicLine,
  getTransLineGenes,
  updateTransLineGene
} from '../api/transgenicLineGeneApi';

import './View.css';

function TransgenicLineGenesView() {
  const firstLabel="Transgenic line"
  const firstType="select"
  const secondLabel="Gene"
  const secondType="select"
	
  const [rows, setRows] = useState([]);
  const [transgenicLines, setTransgenicLines] =
    useState([]);
  const [genes, setGenes] = useState([]);

  const [popupOpen, setPopupOpen] = useState(false);
  const [initialPopupState, setInitialPopupState] = useState({});

  const [editingAssignment, setEditingAssignment] =
    useState(null);

  const [transgenicLineId, setTransgenicLineId] =
    useState('-1');
  const [geneId, setGeneId] = useState('-1');
  const [isSaveEnabled, setIsSaveEnabled] = useState(false);

  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
  const [refreshAfterError, setRefreshAfterError] = useState(false);

  const [deleteArmed, setDeleteArmed] = useState(false);
  const [fieldWarning, setFieldWarning] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [
          assignmentsData,
          linesData,
          genesData
        ] = await Promise.all([
          getTransLineGenes(),
          getTransLines(),
          getGenes()
        ]);

        setRows(
          Array.isArray(assignmentsData)
            ? assignmentsData
            : []
        );

        setTransgenicLines(
          Array.isArray(linesData)
            ? linesData
            : []
        );

        setGenes(
          Array.isArray(genesData)
            ? genesData
            : []
        );
      } catch (err) {
        showError(
          `Could not load transgenic line-gene data.\n${err.message}`
        );
      }
    }

    load();
  }, []);

  useEffect(() => {
    if (!error) {
      return;
    }

    const fadeTimer = setTimeout(() => {
      setErrorFading(true);
    }, ERROR_VISIBLE_TIME);

    const clearTimer = setTimeout(async () => {
      setError('');
      setErrorFading(false);

      if (refreshAfterError) {
        setRefreshAfterError(false);
        await loadAssignments();
      }
    }, ERROR_VISIBLE_TIME + ERROR_FADE_TIME);

    return () => {
      clearTimeout(fadeTimer);
      clearTimeout(clearTimer);
    };
  }, [error, refreshAfterError]);

  function showError(message) {
    setErrorFading(false);
    setError(message);
  }
  
  async function loadAssignments() {
      try {
          const data = await getTransLineGenes();

          setRows(Array.isArray(data) ? data : []);
      } catch (err) {
          showError(`Could not load data.\n${err.message}`);
      }
  }

  function openCreatePopup() {
    const popupState =
      {
          transgenicLineId: '-1',
          geneId: '-1'
      };
    setInitialPopupState(popupState);
    setEditingAssignment(null);
    setTransgenicLineId(popupState.transgenicLineId);
    setGeneId(popupState.geneId);
    setPopupOpen(true);
    setDeleteArmed(false);
  }

  function openEditPopup(
    currentTransgenicLineId,
    currentGeneId
  ) {

    const popupState = {
      transgenicLineId: currentTransgenicLineId,
      geneId: currentGeneId
    };
    setInitialPopupState(popupState);
    setEditingAssignment(popupState);

    setTransgenicLineId(currentTransgenicLineId);
    setGeneId(currentGeneId);
    setPopupOpen(true);
    setDeleteArmed(false);
  }

  function closePopup() {
    setPopupOpen(false);
    setEditingAssignment(null);
    setTransgenicLineId('-1');
    setGeneId('-1');
    setInitialPopupState({});
    setIsSaveEnabled(false);
    setDeleteArmed(false);
  }
  

  async function refreshAfterPopup() {
      closePopup();
      await loadAssignments();
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (
      Number(transgenicLineId) < 0 ||
      Number(geneId) < 0
    ) {
      showError(
        'Transgenic line and gene are required.'
      );
      return;
    }

    const requestBody = {
      transgenicLineId: Number(transgenicLineId),
      geneId: Number(geneId)
    };

    try {
      if (editingAssignment === null) {
        await createTransLineGene(
          requestBody
        );
      } else {
        await updateTransLineGene(
          editingAssignment.transgenicLineId,
          editingAssignment.geneId,
          requestBody
        );
      }

      await refreshAfterPopup();
    } catch (err) {
        const operation =
            editingAssignment === null ? 'create a new entity as' : 'update -';

        showError(
            `Cannot ${operation} ${err.message}`
        );

        closePopup();
        setRefreshAfterError(true);
    }
  }

  async function handleDeleteAllOrOne(deleteAll = false) {
    if (deleteAll) {
      if (transgenicLineId === null) {
        return;
      }
    } else if (editingAssignment === null) {
      return;
    }

    if (!deleteArmed) {
      setDeleteArmed(true);
      return;
    }

    try {
      if (deleteAll) {
        await deleteTransLineGeneByTransgenicLine(
          transgenicLineId
        );
      } else {
        await deleteTransLineGene(
          editingAssignment.transgenicLineId,
          editingAssignment.geneId
        );
      }

      await refreshAfterPopup();
    } catch (err) {
        showError(
            `Could not delete the ${firstLabel}_${secondLabel}.\n${err.message}`
        );

        closePopup();
        setRefreshAfterError(true);
    }
  }

  function onClickTransgenicLine(event, row){
	event.stopPropagation();
	openCreatePopup(row.transgenicLineId);
  }

  return (
    <section>
      <ErrorBanner
        message={error}
        fading={errorFading}
      />

      <h1>Transgenic line : Genes</h1>

      <table>
        <colgroup>
          <col className="id-table-column" />
          <col className="symbol-table-column" />
          <col className="description-table-column" />
        </colgroup>
        <thead>
          <tr>
            <th colSpan={2}>Transgenic line</th>
            <th>Genes</th>
          </tr>
        </thead>

        <tbody>
          {rows.map(row => (
            <tr key={row.transgenicLineId}
                onClick={(event) => onClickTransgenicLine(event, row)}
			>
              <td colSpan={2}>
                <button
                  type="button"
                  className="table-link-button"
                  onClick={(event) => onClickTransgenicLine(event, row)}
                >
                  {row.strainCode}
                  {' — '}
                  {row.transgenicLineName}
                </button>
              </td>

              <td>
                <div className="gene-list">
                  {row.genes.map(gene => (
                    <button
                      type="button"
                      className="gene-chip"
                      key={gene.id}
                      onClick={(event) => {
                        event.stopPropagation();
                        openEditPopup(
                          row.transgenicLineId,
                          gene.id
                        );
                      }}
                    >
                      {gene.symbol}
                    </button>
                  ))}

                </div>
              </td>
            </tr>
          ))}

          <AddNewRecordRow
              entityName='transgenic line with genes'
              onCreate={openCreatePopup}
          />
        </tbody>
      </table>

	  {popupOpen && (
	    <DoubleParamForm
	      entityName="gene assignment"
	      editing={transgenicLineId !== null}

	      firstLabel={firstLabel}
	      firstType="select"
	      firstValue={transgenicLineId}
	      firstPlaceholder="Select transgenic line"
	      firstOptions={transgenicLines.map(line => ({
	        value: line.id,
	        label: `${line.strainCode} — ${line.name}`
	      }))}

	      secondLabel={secondLabel}
	      secondType="select"
	      secondValue={geneId}
	      secondPlaceholder="Select gene"
	      secondOptions={genes.map(gene => ({
	        value: gene.id,
	        label: gene.description
	          ? `${gene.symbol} — ${gene.description}`
	          : gene.symbol
	      }))}

	      onChangeFirstField={value => {
	        setTransgenicLineId(Number(value));
	        setIsSaveEnabled( (initialPopupState.transgenicLineId !== Number(value) || initialPopupState.geneId !== geneId)
	                        && isCurrentInitialPopupStateChosen(value, geneId));
	        setFieldWarning(
	          createFieldWarning(value, geneId, firstLabel, secondLabel, firstType, secondType)
	        );
	      }}

	      onChangeSecondField={value => {
	        setGeneId(Number(value));
	        setIsSaveEnabled((initialPopupState.transgenicLineId !== transgenicLineId || initialPopupState.geneId !== Number(value))
	                        && isCurrentInitialPopupStateChosen(transgenicLineId, value));
	        setFieldWarning(
	          createFieldWarning(transgenicLineId, value, firstLabel, secondLabel, firstType, secondType)
	        );
	      }}

	      firstWarning={fieldWarning}

	      onSubmit={handleSubmit}
	      onDelete={ () => handleDeleteAllOrOne(editingAssignment===null)}
	      onCancel={closePopup}

	      deleteArmed={deleteArmed}
	      isSaveEnabled={isSaveEnabled}
	    />
	  )}
    </section>
  );
}

export default TransgenicLineGenesView;