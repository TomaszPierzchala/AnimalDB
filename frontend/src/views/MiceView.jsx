import {useEffect, useState} from 'react';

import AddNewRecordRow from './AddNewRecordRow'
import WorkInProgressBar from "./WorkInProgress";
import ErrorBanner from '../components/ErrorBanner';
import useFadingError from "../hooks/fadingError.js";

import {createMouse, deleteMouse, getMice, getNextAnimalNumber, updateMouse} from '../api/miceApi';
import {getStrains} from "../api/strainApi.js";

import './View.css';

const COLUMNS = [
    { name: 'animalNumber', label: 'Animal number' },
    { name: 'sex', label: 'Sex' },
    { name: 'strainId', label: 'Strain' },
    { name: 'transgenicLineId', label: 'Transgenic line' },
    { name: 'labProcedureId', label: 'Lab procedure' },
    { name: 'motherId', label: 'Mother' },
    { name: 'fatherId', label: 'Father' },
    { name: 'birthDate', label: 'Birth date' },
    { name: 'deathDate', label: 'Death date' },
    { name: 'room', label: 'Room' },
    { name: 'rack', label: 'Rack' },
    { name: 'cage', label: 'Cage' },
    { name: 'origin', label: 'Origin' },
    { name: 'note', label: 'Note' }
];
const REQUIRED_COLUMNS = [
    'animalNumber',
    'sex',
    'strainId'
];
const EDITABLE_MOUSE_FIELDS = [
    'animalNumber',
    'sex',
    'strainId',
    'transgenicLineId',
    'labProcedureId',
    'motherId',
    'fatherId',
    'birthDate',
    'deathDate',
    'room',
    'rack',
    'cage',
    'origin',
    'note'
];
const PAGE_SIZE = 20;

function createEmptyMouse(animalNumber) {
    return {
        animalNumber,
        sex: 'M',
        strainId: '',
        transgenicLineId: null,
        labProcedureId: null,
        motherId: null,
        fatherId: null,
        birthDate: '',
        deathDate: '',
        room: '',
        rack: '',
        cage: '',
        origin: '',
        note: ''
    };
}

function toMouseRequest(mouse) {
    return {
        animalNumber: mouse.animalNumber,
        sex: mouse.sex,
        strainId: mouse.strainId,
        transgenicLineId: mouse.transgenicLineId,
        labProcedureId: mouse.labProcedureId,
        motherId: mouse.motherId,
        fatherId: mouse.fatherId,
        birthDate: mouse.birthDate,
        deathDate: mouse.deathDate,
        room: mouse.room,
        rack: mouse.rack,
        cage: mouse.cage,
        origin: mouse.origin,
        note: mouse.note
    };
}

function MiceView() {

    // =========================
    // STATE
    // =========================

    const [mice, setMice] = useState([]);
    const [addingNewMouse, setAddingNewMouse] = useState(false);
    const [newMouse, setNewMouse] = useState(null);

    const [editingMouseId, setEditingMouseId] = useState(null);
    const [editedMouse, setEditedMouse] = useState(null);
    const [strainOptions, setStrainOptions] = useState([]);

    const [selectedColumns, setSelectedColumns] =
        useState(() => {
            const savedColumns =
                localStorage.getItem('miceSelectedColumns');

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
                    `Could not read saved 'miceSelectedColumns':`,
                    err.message
                );
                return REQUIRED_COLUMNS;
            }
        });

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [sortBy, setSortBy] =
        useState('animalNumber');

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

    const visibleColumns =
        COLUMNS.filter(column =>
            selectedColumns.includes(column.name)
        );
    const originalEditedMouse =
        mice.find(mouse => mouse.id === editingMouseId);

    const editedMouseChanged =
        editedMouse !== null &&
        originalEditedMouse !== undefined &&
        EDITABLE_MOUSE_FIELDS.some(
            fieldName =>
                editedMouse[fieldName] !==
                originalEditedMouse[fieldName]
        );


    // =========================
    // EFFECTS
    // =========================

    useEffect(() => {
        loadMice();
    }, [page, sortBy, direction]);

    useEffect(() => {
        localStorage.setItem(
            'miceSelectedColumns',
            JSON.stringify(selectedColumns)
        );
    }, [selectedColumns]);


    // =========================
    // DATA LOADING
    // =========================

    async function loadMice() {
        try {
            const data = await getMice({
                page,
                size: PAGE_SIZE,
                sortBy,
                direction
            });

            setMice(data.content ?? []);
            setTotalPages(data.page.totalPages ?? 0);
            setTotalElements(data.page.totalElements ?? 0);

        } catch (err) {
            showError(
                `Could not load mice.\n${err.message}`
            );
        }
    }


    // =========================
    // ADDING
    // =========================


    async function startAddingMouse() {
        try {
            const nextAnimalNumber =
                await getNextAnimalNumber();

            setNewMouse(
                createEmptyMouse(nextAnimalNumber)
            );

            setAddingNewMouse(true);

        } catch (err) {
            showError(
                `Could not determine next animal number.\n${err.message}`
            );
        }
    }

    function changeNewMouseField(fieldName, value) {
        setNewMouse(current => ({
            ...current,
            [fieldName]: value
        }));
    }

    async function acceptNewMouse() {
        try {
            await createMouse(newMouse);

            setAddingNewMouse(false);
            setNewMouse(null);

            await loadMice();

        } catch (err) {
            showError(
                `Could not create mouse.\n${err.message}`
            );
        }
    }

    function cancelAddingMouse() {
        setAddingNewMouse(false);
        setNewMouse(null);
    }


    // =========================
    // EDITING
    // =========================

    async function startEditingMouse(mouse) {
        try {
            const strains = await getStrains();

            setStrainOptions(strains);
            setEditingMouseId(mouse.id);
            setEditedMouse({ ...mouse });
        } catch (err) {
            showError(`Could not load strains.\n${err.message}`);
        }
    }

    function changeEditedMouseField(fieldName, value) {
        setEditedMouse(current => ({
            ...current,
            [fieldName]: value
        }));
    }

    function cancelEditingMouse() {
        setEditingMouseId(null);
        setEditedMouse(null);
        setStrainOptions([]);
    }

    async function saveEditedMouse() {
        try {
            await updateMouse(
                editingMouseId,
                toMouseRequest(editedMouse)
            );

            cancelEditingMouse();

            await loadMice();

        } catch (err) {
            showError(
                `Could not update mouse.\n${err.message}`
            );
        }
    }

    async function deleteEditedMouse() {
        try {
            await deleteMouse(editingMouseId);

            cancelEditingMouse();

            await loadMice();

        } catch (err) {
            showError(
                `Could not delete mouse.\n${err.message}`
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

    function displayValue(mouse, columnName) {
        if (columnName === 'strainId') {
            return `${mouse.strainCode} — ${mouse.strainName}`;
        }

        const value = mouse[columnName];

        return value ?? '—';
    }

    function renderEditableCell(column) {
        if (column.name === 'strainId') {
            return (
                <select
                    className="mice-select"
                    value={editedMouse.strainId}
                    onClick={event => event.stopPropagation()}
                    onChange={event =>
                        changeEditedMouseField(
                            'strainId',
                            Number(event.target.value)
                        )
                    }
                >
                    {strainOptions.map(strain => (
                        <option
                            key={strain.id}
                            value={strain.id}
                        >
                            {strain.code} — {strain.name}
                        </option>
                    ))}
                </select>
            );
        }
        if (column.name === 'sex') {
            return (
                <select
                    className="mice-select"
                    value={editedMouse.sex}
                    onClick={event => event.stopPropagation()}
                    onChange={event =>
                        changeEditedMouseField('sex', event.target.value)
                    }
                >
                    <option value="M">M</option>
                    <option value="F">F</option>
                </select>
            );
        }
        if (
            column.name === 'birthDate' ||
            column.name === 'deathDate'
        ) {
            return (
                <input
                    type="date"
                    value={editedMouse[column.name] ?? ''}
                    onClick={event => event.stopPropagation()}
                    onChange={event =>
                        changeEditedMouseField(
                            column.name,
                            event.target.value
                        )
                    }
                />
            );
        }
        if (column.name === 'animalNumber') {
            return (
                <input
                    type="number"
                    value={editedMouse.animalNumber}
                    onClick={event => event.stopPropagation()}
                    onChange={event =>
                        changeEditedMouseField(
                            'animalNumber',
                            Number(event.target.value)
                        )
                    }
                />
            );
        }

        return (
            <input
                value={editedMouse[column.name] ?? ''}
                onClick={event => event.stopPropagation()}
                onChange={event =>
                    changeEditedMouseField(
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

            <div><h1 className="mice-title">Mice</h1>

                <details className="column-selector">
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
                    {mice.map(mouse => (
                        <tr
                            key={mouse.id}
                            className="clickable-row"
                            onClick={() => startEditingMouse(mouse)}
                        >
                            <td className="id-column">
                                {mouse.id}
                            </td>

                            {visibleColumns.map(column => (
                                <td key={column.name} className="mice-data-column">

                                    {editingMouseId === mouse.id
                                        ? renderEditableCell(column)
                                        : displayValue(mouse, column.name)
                                    }

                                </td>
                            ))}
                        </tr>
                    ))}
                    {addingNewMouse && (
                        <tr className="new-mouse-row">

                            <td className="id-column">
                                —
                            </td>

                            {visibleColumns.map(column => (
                                <td
                                    key={column.name}
                                    className="mice-data-column"
                                >
                                    <input
                                        value={
                                            newMouse[column.name] ?? ''
                                        }
                                        onChange={event =>
                                            changeNewMouseField(
                                                column.name,
                                                event.target.value
                                            )
                                        }
                                    />
                                </td>
                            ))}

                        </tr>
                    )}
                    </tbody>

                </table>
            </div>
            <table className="mice-add-table">
                <tbody>

                {editingMouseId !== null ? (

                    <tr className="mouse-edit-actions-row">

                        <td className="mouse-edit-actions-cell">

                            <button
                                type="button"
                                onClick={deleteEditedMouse}
                            >
                                Delete
                            </button>

                            <div className="mouse-edit-main-actions">

                                <button
                                    type="button"
                                    disabled={!editedMouseChanged}
                                    onClick={saveEditedMouse}
                                >
                                    Save
                                </button>

                                <button
                                    type="button"
                                    onClick={cancelEditingMouse}
                                >
                                    Cancel
                                </button>

                            </div>

                        </td>

                    </tr>

                ) : (

                    <AddNewRecordRow
                        entityName="mouse"
                        onCreate={
                            addingNewMouse
                                ? acceptNewMouse
                                : startAddingMouse
                        }
                        acceptText={
                            addingNewMouse
                                ? (
                                    <>
                                        Click here to{' '}
                                        <span className="accept-text">
                                accept
                            </span>
                                        {' '}new mouse...
                                    </>
                                )
                                : null
                        }
                        action={
                            addingNewMouse
                                ? (
                                    <button
                                        type="button"
                                        onClick={event => {
                                            event.stopPropagation();
                                            cancelAddingMouse();
                                        }}
                                    >
                                        Cancel
                                    </button>
                                )
                                : null
                        }
                        colSpan={1}
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
                    {totalElements} mice
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

export default MiceView;