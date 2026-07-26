import { useEffect, useState } from 'react';

import ErrorBanner from '../components/ErrorBanner';

import { getGenes } from '../api/geneApi';
import { getTransLines } from '../api/transgenicLineApi';
import NewDoubleParamForm from './NewDoubleParamForm'
import {createFieldWarning} from '../warnings/popupWarning'

import {
  createTransLineGene,
  deleteTransLineGene,
  getTransLineGenes,
  updateTransLineGene
} from '../api/transgenicLineGeneApi';

import './View.css';

function TransgenicLineGenesView() {
  const firstName="Transgenic line"
  const firstType="select"
  const secondName="Gene"
  const secondType="select"
	
  const [rows, setRows] = useState([]);
  const [transgenicLines, setTransgenicLines] =
    useState([]);
  const [genes, setGenes] = useState([]);

  const [popupOpen, setPopupOpen] = useState(false);

  const [editingAssignment, setEditingAssignment] =
    useState(null);

  const [transgenicLineId, setTransgenicLineId] =
    useState('-1');

  const [geneId, setGeneId] = useState('-1');

  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
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

  function showError(message) {
    setErrorFading(false);
    setError(message);
  }
  
  async function loadAssignments() {
    const data = await getTransLineGenes();

    setRows(Array.isArray(data) ? data : []);
  }

  function openCreatePopup(preselectedLineId = '-1') {
    setEditingAssignment(null);
    setTransgenicLineId(String(preselectedLineId));
    setGeneId('-1');
    setPopupOpen(true);
	setDeleteArmed(false);
  }

  function openEditPopup(
    currentTransgenicLineId,
    currentGeneId
  ) {
    setEditingAssignment({
      transgenicLineId: currentTransgenicLineId,
      geneId: currentGeneId
    });

    setTransgenicLineId(
      String(currentTransgenicLineId)
    );

    setGeneId(String(currentGeneId));
    setPopupOpen(true);
	setDeleteArmed(false);
  }

  function closePopup() {
    setPopupOpen(false);
    setEditingAssignment(null);
    setTransgenicLineId('-1');
    setGeneId('-1');
	setDeleteArmed(false);
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

      closePopup();
      await loadAssignments();
    } catch (err) {
      showError(
        `Could not save the assignment.\n${err.message}`
      );
    }
  }

  async function handleDelete() {
    if (editingAssignment === null) {
      return;
    }

	if (!deleteArmed) {
	      setDeleteArmed(true);
	      return;
	}

    try {
      await deleteTransLineGene(
        editingAssignment.transgenicLineId,
        editingAssignment.geneId
      );

      closePopup();
      await loadAssignments();
    } catch (err) {
      showError(
        `Could not delete the assignment.\n${err.message}`
      );
    }
  }

/*
  const originalFirstValue =
    entity === null
      ? initialFirstValue
      : String(entity[firstEditName] ?? initialFirstValue);

  const originalSecondValue =
    entity === null
      ? ''
      : entity[secondName] ?? '';

  const hasChanges =
    entity === null ||
    firstValue !== originalFirstValue ||
    secondValue !== originalSecondValue;
*/
  return (
    <section>
      <ErrorBanner
        message={error}
        fading={errorFading}
      />

      <h1>Transgenic line - Genes</h1>

      <table>
        <thead>
          <tr>
            <th>Transgenic line</th>
            <th>Genes</th>
          </tr>
        </thead>

        <tbody>
          {rows.map(row => (
            <tr key={row.transgenicLineId}>
              <td>
                <button
                  type="button"
                  className="table-link-button"
                  onClick={() =>
                    openCreatePopup(
                      row.transgenicLineId
                    )
                  }
                >
                  {row.strainCode}
                  {' - '}
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
                      onClick={() =>
                        openEditPopup(
                          row.transgenicLineId,
                          gene.id
                        )
                      }
                    >
                      {gene.symbol}
                    </button>
                  ))}

                  <button
                    type="button"
                    className="add-gene-button"
                    onClick={() =>
                      openCreatePopup(
                        row.transgenicLineId
                      )
                    }
                  >
                    +
                  </button>
                </div>
              </td>
            </tr>
          ))}

          <tr
            className="empty-row"
            onClick={() => openCreatePopup()}
          >
            <td colSpan={2}>
              + Add transgenic line-gene assignment
            </td>
          </tr>
        </tbody>
      </table>

	  {popupOpen && (
	    <NewDoubleParamForm
	      entityName="gene assignment"
	      editing={editingAssignment !== null}

	      firstName={firstName}
	      firstType="select"
	      firstValue={transgenicLineId}
	      firstPlaceholder="Select transgenic line"
	      firstOptions={transgenicLines.map(line => ({
	        value: line.id,
	        label: `${line.strainCode} - ${line.name}`
	      }))}

	      secondName="Gene"
	      secondType="select"
	      secondValue={geneId}
	      secondPlaceholder="Select gene"
	      secondOptions={genes.map(gene => ({
	        value: gene.id,
	        label: gene.description
	          ? `${gene.symbol} - ${gene.description}`
	          : gene.symbol
	      }))}

	      onChangeFirstField={value => {
	        setTransgenicLineId(value);
	        setFieldWarning(
			  createFieldWarning(value, geneId, firstName, secondName, firstType, secondType)
	        );
	      }}

	      onChangeSecondField={value => {
	        setGeneId(value);
	        setFieldWarning(
	          createFieldWarning(transgenicLineId, value, firstName, secondName, firstType, secondType)
	        );
	      }}

	      firstWarning={fieldWarning}

	      onSubmit={handleSubmit}
	      onDelete={handleDelete}
	      onCancel={closePopup}

	      deleteArmed={deleteArmed}
	      hasChanges={true} //{hasChanges}
	    />
	  )}
    </section>
  );
}

export default TransgenicLineGenesView;