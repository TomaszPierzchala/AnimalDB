import {useEffect, useState} from 'react';

import AddNewRecordRow from './AddNewRecordRow';
import EditRecordActionsRow from './EditRecordActionsRow';
import WorkInProgressBar from "./WorkInProgress";
import ErrorBanner from '../components/ErrorBanner';
import MatingCandidatesInput from "../components/MatingCandidatesInput";
import useFadingError from "../hooks/fadingError";
import {useEditKeyDown} from '../hooks/useEditKeyDown';
import {getTransLines} from '../api/transgenicLineApi';
import {createMating, deleteMating, getMatings, updateMating} from '../api/matingApi';
import {todayAsInputDate} from '../utils/timeUtils'
import {conditionalClearStorage} from '../utils/devUtils';

import './View.css';

const COLUMNS = [
    {name: 'femaleAnimalNumber', label: 'Female ♀'},
    {name: 'maleAnimalNumber', label: 'Male ♂'},
    {name: 'matingDate', label: 'Mating date'},
    {name: 'plugDate', label: 'Plug date'},
    {name: 'pregnancyDate', label: 'Pregnancy date'},
    {name: 'birthDate', label: 'Birth date'},
    {name: 'weaningDate', label: 'Weaning date'},
    {name: 'bornCount', label: 'Born #'},
    {name: 'aliveCount', label: 'Alive #'},
    {name: 'femaleCount', label: 'Females #'},
    {name: 'maleCount', label: 'Males #'},
    {name: 'note', label: 'Note'}
];
const REQUIRED_COLUMNS = [
    'femaleAnimalNumber',
    'maleAnimalNumber'
];
const EDITABLE_MOUSE_FIELDS = [
    'femaleAnimalNumber',
    'maleAnimalNumber',
    'matingDate',
    'plugDate',
    'pregnancyDate',
    'birthDate',
    'weaningDate',
    'bornCount',
    'aliveCount',
    'femaleCount',
    'maleCount',
    'note'
];
const PAGE_SIZE = 20;

function createEmptyMating() {
    return {
        id: null,
        femaleAnimalNumber: null,
        maleAnimalNumber: null,
        femaleStrainTransgenicLineDetails: null,
        maleStrainTransgenicLineDetails: null,

        matingDate: '',
        plugDate: '',
        pregnancyDate: '',
        birthDate: '',
        weaningDate: '',

        bornCount: null,
        aliveCount: null,
        femaleCount: null,
        maleCount: null,

        note: ''
    };
}

function toMatingRequest(mating) {
    return {
        femaleAnimalNumber: mating.femaleAnimalNumber,
        maleAnimalNumber: mating.maleAnimalNumber,
        matingDate: mating.matingDate,
        plugDate: mating.plugDate,
        pregnancyDate: mating.pregnancyDate,
        birthDate: mating.birthDate,
        weaningDate: mating.weaningDate,
        bornCount: mating.bornCount,
        aliveCount: mating.aliveCount,
        femaleCount: mating.femaleCount,
        maleCount: mating.maleCount,
        note: mating.note
    };
}

function MatingView() {

    // =========================
    // STATE
    // =========================
    const [strainTransgenicLines, setStrainTrLines] = useState([]);
    const [strainTransgenicLineId, setStrainTrLineId] = useState(0)

    const [matings, setMatings] = useState([]);
    const [editedMating, setEditedMating] = useState(null);


    const STORAGE_KEY = 'matingSelectedColumns';
    const CLEAR_STORAGE = false

    const [selectedColumns, setSelectedColumns] =
        useState(() => {

            conditionalClearStorage(STORAGE_KEY, CLEAR_STORAGE);

            const savedColumns =
                localStorage.getItem(STORAGE_KEY);

            if (!savedColumns) {
                return REQUIRED_COLUMNS;
            }

            try {
                const parsed = JSON.parse(savedColumns);

                if (!Array.isArray(parsed)) {
                    return REQUIRED_COLUMNS;
                }

                const validColumnNames =
                    COLUMNS.map(column => column.name);

                const validParsed = parsed.filter(name =>
                    validColumnNames.includes(name)
                );
                const validParsedAndRequired = [...validParsed, ...REQUIRED_COLUMNS];

                return [...new Set(validParsedAndRequired)]; // UNIQUE

            } catch(err) {
                console.error(
                    `Could not read saved 'matingSelectedColumns':`,
                    err.message
                );
                return REQUIRED_COLUMNS;
            }
        });

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [sortBy, setSortBy] =
        useState('femaleAnimalNumber');

    const [direction, setDirection] =
        useState('asc');

    const {
        error,
        errorFading,
        showError
    } = useFadingError();


    // =========================
    // DERIVED VALUES
    // =========================

    const isAddingMating = editedMating?.id === null;
    const isEditingMating = editedMating?.id != null;

    const visibleColumns =
        COLUMNS.filter(column =>
            selectedColumns.includes(column.name)
        );

    const originalEditedMating =
        matings.find(mating => mating.id === editedMating?.id);

    const editedMatingChanged =
        originalEditedMating !== undefined &&
        EDITABLE_MOUSE_FIELDS.some(
            fieldName =>
                editedMating[fieldName] !==
                originalEditedMating[fieldName]
        );

    useEditKeyDown({
        isEditing: isEditingMating,
        hasChanges: editedMatingChanged,
        isAdding: isAddingMating,
        onSave: saveEditedMating,
        onAccept: acceptNewMating,
        onCancel: clearEditedMating
    });

    // =========================
    // EFFECTS
    // =========================

    // === temporary ===
    useEffect(() => {
        console.log("New strainTrLine =", strainTransgenicLineId);
    }, [strainTransgenicLineId]);
    useEffect(() => {
        console.log("strainTrLines = ", strainTransgenicLines);
    }, [strainTransgenicLines]);
    // ==== ==== ====

    useEffect(() => {
        async function loadStrainTransgenicLines() {
            try{
                const strainData = await getTransLines();
                setStrainTrLines(Array.isArray(strainData)
                    ? strainData
                    : [])
            } catch (err) {
                showError(
                    `Could not load initial strainTransgenicLines.\n${err.message}`
                );
            }
        }

        loadStrainTransgenicLines();
    }, []);
// ===============================================================

    useEffect(() => {
        loadMatings();
    }, [page, sortBy, direction]);

    useEffect(() => {
        localStorage.setItem(
            STORAGE_KEY,
            JSON.stringify(selectedColumns)
        );
    }, [selectedColumns]);


    // =========================
    // DATA LOADING
    // =========================

    async function loadMatings() {
        try {
            const data = await getMatings({
                page,
                size: PAGE_SIZE,
                sortBy,
                direction
            });

            setMatings(data.content ?? []);
            setTotalPages(data.page.totalPages ?? 0);
            setTotalElements(data.page.totalElements ?? 0);

        } catch (err) {
            showError(
                `Could not load matings.\n${err.message}`
            );
        }
    }


    // =========================
    // ADDING
    // =========================

    async function handelSelectedStrainTransgenicLine(selected) {
        setStrainTrLineId(selected);
    }

    async function startAddingMating() {
        try {
            const newMating = createEmptyMating();

            setEditedMating(newMating);

        } catch (err) {
            showError(
                `Could not start adding mating.\n${err.message}`
            );
        }
    }

    async function acceptNewMating() {
        try {
            await createMating(editedMating);

            clearEditedMating();
            await loadMatings();

        } catch (err) {
            showError(
                `Could not create mating.\n${err.message}`
            );
        }
    }

    // =========================
    // EDITING
    // =========================

    async function startEditingMating(mating) {
        try {
            setEditedMating({ ...mating });

        } catch (err) {
            showError(
                `Could not start editing mating.\n${err.message}`
            );
        }
    }

    function changeEditedMatingField(fieldName, value) {
        setEditedMating(current => ({
            ...current,
            [fieldName]: value
        }));
    }

    function clearEditedMating() {
        setEditedMating(null);
        setStrainTrLineId(0);
    }

    async function saveEditedMating() {
        try {
            await updateMating(
                editedMating.id,
                toMatingRequest(editedMating)
            );

            clearEditedMating();

            await loadMatings();

        } catch (err) {
            showError(
                `Could not update mating.\n${err.message}`
            );
        }
    }

    async function deleteEditedMating() {
        try {
            await deleteMating(editedMating.id);

            clearEditedMating();

            await loadMatings();

        } catch (err) {
            showError(
                `Could not delete mating.\n${err.message}`
            );
        }
    }


    // =========================
    // COLUMNS / SORTING
    // =========================

    function toggleColumn(columnName) {
        setSelectedColumns(currentColumns => {

            if (currentColumns.includes(columnName)) {
                return currentColumns.filter(
                    name => name !== columnName
                );
            }

            return [
                ...currentColumns,
                columnName
            ];
        });
    }

    function handleSort(columnName) {

        if (sortBy === columnName) {
            setDirection(
                current =>
                    current === 'asc'
                        ? 'desc'
                        : 'asc'
            );
        } else {
            setSortBy(columnName);
            setDirection('asc');
        }

        setPage(0);
    }

    function sortIndicator(columnName) {

        if (sortBy !== columnName) {
            return '';
        }

        return direction === 'asc'
            ? ' ▲'
            : ' ▼';
    }


    // =========================
    // RENDER HELPERS
    // =========================

    function displayValue(mating, columnName) {
        const value = mating[columnName];

        if (columnName === 'femaleAnimalNumber' || columnName === 'maleAnimalNumber') {
            const details = (columnName === 'femaleAnimalNumber')?
                mating.femaleStrainTransgenicLineDetails
                : mating.maleStrainTransgenicLineDetails;

            if (!details) {
                return value ?? '—';
            }

            return (
                <>
                    <strong>{value ?? '—'}</strong>
                    <small>
                        {` : ${details.strainCode ?? '—'} - ${details.strainName ?? '—'}/${details.name ?? '—'}`}
                    </small>
                </>
            );
        }

        return value ?? '—';
    }

    function renderEditableCell(column) {
        if (column.name === 'femaleAnimalNumber') {
            return (
                <MatingCandidatesInput
                    value={editedMating.femaleAnimalNumber}
                    sex="F"
                    transgenicLineId={strainTransgenicLineId}
                    onChange={value =>
                        changeEditedMatingField(
                            'femaleAnimalNumber',
                            value
                        )
                    }
                />
            );
        }

        if (column.name === 'maleAnimalNumber') {
            return (
                <MatingCandidatesInput
                    value={editedMating.maleAnimalNumber}
                    sex="M"
                    transgenicLineId={strainTransgenicLineId}
                    onChange={value =>
                        changeEditedMatingField(
                            'maleAnimalNumber',
                            value
                        )
                    }
                />
            );
        }
        if (
            column.name === 'matingDate' ||
            column.name === 'plugDate' ||
            column.name === 'pregnancyDate' ||
            column.name === 'birthDate' ||
            column.name === 'weaningDate'
        ) {
            return (
                <input
                    type="date"
                    max={todayAsInputDate()}
                    value={editedMating[column.name] ?? ''}
                    onClick={event => event.stopPropagation()}
                    onChange={event =>
                        changeEditedMatingField(
                            column.name,
                            event.target.value
                        )
                    }
                />
            );
        }
        if (
            column.name === 'bornCount' ||
            column.name === 'aliveCount' ||
            column.name === 'femaleCount' ||
            column.name === 'maleCount'
        ) {
            return (
                <input
                    type="number"
                    min="0"
                    value={editedMating[column.name] ?? ''}
                    onClick={event => event.stopPropagation()}
                    onChange={event =>
                        changeEditedMatingField(
                            column.name,
                            event.target.value === ''
                                ? null
                                : Number(event.target.value)
                        )
                    }
                />
            );
        }

        return (
            <input
                value={editedMating[column.name] ?? ''}
                onClick={event => event.stopPropagation()}
                onChange={event =>
                    changeEditedMatingField(
                        column.name,
                        event.target.value
                    )
                }
            />
        );
    }

    return (
        <section>

            <ErrorBanner
                message={error}
                fading={errorFading}
            />

            <div><h1 className="mice-title">Mating</h1>

                <details
                    className="column-selector"
                    onKeyDown={event => {
                        if (event.key === 'Escape') {
                            event.currentTarget.open = false;
                        }
                    }}
                >
                    <summary>Displayed columns</summary>

                    <div className="column-selector-menu">
                        {COLUMNS.map(column => (
                            <label key={column.name}>
                                <input
                                    type="checkbox"
                                    checked={selectedColumns.includes(column.name)}
                                    onChange={() => toggleColumn(column.name)}
                                    disabled={REQUIRED_COLUMNS.includes(column.name)}
                                />
                                {column.label}
                            </label>
                        ))}
                    </div>
                </details>
            </div>

            <div className="mice-table-scroll">
                <table className="mice-table">

                    <thead>
                    <tr>
                        <th className="id-column">
                            ID
                        </th>

                        {visibleColumns.map(column => (
                            <th
                                key={column.name}
                                className="mice-data-column"
                                onClick={() => handleSort(column.name)}
                            >
                                {column.label}
                                {sortIndicator(column.name)}
                            </th>
                        ))}
                    </tr>
                    </thead>

                    <tbody>
                    {matings.map(mating => (
                        <tr
                            key={mating.id}
                            className="clickable-row"
                            onClick={() => startEditingMating(mating)}
                        >
                            <td className="id-column">
                                {mating.id}
                            </td>

                            {visibleColumns.map(column => (
                                <td key={column.name} className="mice-data-column">

                                    {editedMating?.id === mating.id
                                        ? renderEditableCell(column)
                                        : displayValue(mating, column.name)
                                    }

                                </td>
                            ))}
                        </tr>
                    ))}
                    {isAddingMating && (
                        <tr className="new-mouse-row">

                            <td className="id-column">
                                —
                            </td>

                            {visibleColumns.map(column => (
                                <td key={column.name} className="mice-data-column">
                                    {renderEditableCell(column)}
                                </td>
                            ))}

                        </tr>
                    )}
                    </tbody>

                </table>
            </div>
            <table className="mice-add-table">
                <tbody>

                {isEditingMating ? (
                    <EditRecordActionsRow
                        canSave={editedMatingChanged}
                        onSave={saveEditedMating}
                        onDelete={deleteEditedMating}
                        onCancel={clearEditedMating}
                    />
                ) : (
                    <AddNewRecordRow
                        entityName="mating"
                        onCreate={
                            isAddingMating
                                ? acceptNewMating
                                : startAddingMating
                        }
                        acceptText={
                            isAddingMating
                                ? (
                                    <>
                                        Click here to{' '}
                                        <span className="accept-text">
                                            accept
                                        </span>
                                        {' '}new mating...
                                    </>
                                )
                                : null
                        }
                        cancelAction={
                            isAddingMating
                                ? clearEditedMating
                                : null
                        }
                        colSpan={1}
                        onSelect={handelSelectedStrainTransgenicLine}
                        selectOptions={strainTransgenicLines}
                    />
                )}
                </tbody>
            </table>
            <div className="pagination">

                <button
                    type="button"
                    disabled={page === 0}
                    onClick={() =>
                        setPage(current => current - 1)
                    }
                >
                    Previous
                </button>


                <span>
          Page {totalPages === 0 ? 0 : page + 1}
                    {' '}of{' '}
                    {totalPages}
                    {' — '}
                    {totalElements} matings
        </span>


                <button
                    type="button"
                    disabled={
                        totalPages === 0 ||
                        page >= totalPages - 1
                    }
                    onClick={() =>
                        setPage(current => current + 1)
                    }
                >
                    Next
                </button>

            </div>
            <WorkInProgressBar/>
        </section>
    );
}

export default MatingView;