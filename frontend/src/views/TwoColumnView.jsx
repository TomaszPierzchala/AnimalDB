import { useEffect, useState } from 'react';

import ErrorBanner from '../components/ErrorBanner';
import useFadingError from "../hooks/fadingError.js";
import { firstCapital } from '../utils/textUtils';
import { createFieldWarning, isCurrentInitialPopupStateChosen } from '../warnings/popupWarning'
import { ZERO, FIRST, SECOND, VAR_MAX_LENGTH} from '../utils/const';
import DoubleParamForm from './DoubleParamForm';
import TwoColumnTable from './TwoColumnTable';

import './View.css';

function TwoColumnView({
  entityName,

  firstName,
  firstName2 = null,
  firstLabel = firstCapital(firstName),
  firstMaxLength = 50,

  secondName,
  secondLabel = firstCapital(secondName),
  secondMaxLength = VAR_MAX_LENGTH,

  warningKey = FIRST | SECOND,
  dropDown = ZERO,

  firstRequestName = firstName,
  firstEditName = firstName,
  firstInputType = 'text',

  getSubEntityApi,
  subEntityLabelName = 'code',
  subEntitySecondLabelName = null,

  createApi,
  getApi,
  updateApi,
  deleteApi
}) {
  const initialFirstValue =
    firstInputType === 'select' ? '-1' : '';
  const {
    error,
    errorFading,
    showError
  } = useFadingError();

  const [records, setRecords] = useState([]);
  const [firstOptions, setFirstOptions] = useState([]);

  const [popupOpen, setPopupOpen] = useState(false);
  const [initialPopupState, setInitialPopupState] = useState({});
  const [entity, setEntity] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [firstValue, setFirstValue] =
    useState(initialFirstValue);

  const [secondValue, setSecondValue] = useState('');
  const [isSaveEnabled, setIsSaveEnabled] = useState(false);
  const [fieldWarning, setFieldWarning] = useState('');

  useEffect(() => {
    async function load() {
      await loadRecords();
    }

    load();
  }, [getApi]);

  useEffect(() => {
    if (!getSubEntityApi) {
      return;
    }

    async function loadOptions() {
      try {
        const data = await getSubEntityApi();

        const options = Array.isArray(data)
          ? data.map(item => ({
              value: String(item.id),
              label: subEntitySecondLabelName
                ? `${item[subEntityLabelName]} — ${item[subEntitySecondLabelName]}`
                : item[subEntityLabelName]
            }))
          : [];

        setFirstOptions(options);
      } catch (err) {
        showError(
          `Could not load available options.\n${err.message}`
        );
      }
    }

    loadOptions();
  }, [
    getSubEntityApi,
    subEntityLabelName,
    subEntitySecondLabelName
  ]);

  async function loadRecords() {
    try {
      const data = await getApi();

      setRecords(Array.isArray(data) ? data : []);
    } catch (err) {
      showError(`Could not load data.\n${err.message}`);
    }
  }

    function currentInitaialPopupState(entity = null) {
        return {
            firstValue: entity === null
                ? initialFirstValue
                : String(entity[firstEditName] ?? initialFirstValue),
            secondValue: entity === null
                ? ''
                : entity[secondName] ?? ''
        };
    }

  function openCreatePopup() {
    const popupState = currentInitaialPopupState();

    setInitialPopupState(popupState);

    const {
       firstValue: first,
       secondValue: second
    } = popupState;

    setEntity(null);
    setFirstValue(first);
    setSecondValue(second);
    setDeleteArmed(false);
    setFieldWarning(createWarning(first, second));
    setPopupOpen(true);
  }

  function openEditPopup(selectedEntity) {
    const popupState = currentInitaialPopupState(selectedEntity);

    setInitialPopupState(popupState);

    const {
      firstValue: first,
      secondValue: second
    } = popupState;

    setEntity(selectedEntity);
    setFirstValue(first);
    setSecondValue(second);
    setDeleteArmed(false);
    setFieldWarning(createWarning(first, second));
    setPopupOpen(true);
  }

  function closePopup() {
    setPopupOpen(false);
    setEntity(null);
    setFirstValue(initialFirstValue);
    setSecondValue('');
    setInitialPopupState({});
    setIsSaveEnabled(false);
    setDeleteArmed(false);
    setFieldWarning('');
  }

  async function refreshAfterPopup() {
    closePopup();
    await loadRecords();
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const validationMessage =
      createWarning(firstValue, secondValue);

    if (validationMessage) {
      setFieldWarning(validationMessage);
      return;
    }

    if (entity !== null && !isSaveEnabled) {
      return;
    }

    const requestBody = {
      [firstRequestName]:
        firstInputType === 'select'
          ? Number(firstValue)
          : firstValue,

      [secondName]: secondValue
    };

    try {
      if (entity === null) {
        await createApi(requestBody);
      } else {
        await updateApi(entity.id, requestBody);
      }

      await refreshAfterPopup();
    } catch (err) {
      const operation =
        entity === null ? 'create a new' : 'update the';

      showError(
        `Could not ${operation} ${entityName.toLowerCase()}.\n${err.message}`,
          entity !== null
              ? async () => {
                await loadRecords();
              }
              : null
      );

      if (entity !== null) {
        closePopup();
      }
    }
  }

  async function handleDelete() {
    if (entity === null) {
      return;
    }

    if (!deleteArmed) {
      setDeleteArmed(true);
      return;
    }

    try {
      await deleteApi(entity.id);
      await refreshAfterPopup();
    } catch (err) {
      showError(
        `Could not delete the ${entityName.toLowerCase()}.\n${err.message}`,
          async () => {
            await loadRecords();
          }
      );

      closePopup();
    }
  }
  function createWarning(first, second) {
    return createFieldWarning(
		first, second,
		firstLabel, secondLabel, firstInputType, 'text',
		warningKey, firstMaxLength, secondMaxLength)
  }

  return (
    <section>
      <ErrorBanner
        message={error}
        fading={errorFading}
      />

      <h1>{entityName}s</h1>

      <TwoColumnTable
        records={records}
        onEdit={openEditPopup}
        onCreate={openCreatePopup}
        entityName={entityName}
        firstName={firstName}
        firstName2={firstName2}
        firstLabel={firstLabel}
        secondName={secondName}
        secondLabel={secondLabel}
      />

      {popupOpen && (
        <DoubleParamForm
          editing={entity !== null}
          entityName={entityName}

          firstLabel={firstLabel}
          firstType={firstInputType}
          firstValue={firstValue}
          firstMaxLength={firstMaxLength}
          firstOptions={firstOptions}
          // firstPlaceholder=''
          firstWarning={fieldWarning}

          secondLabel={secondLabel}
          secondValue={secondValue}
          secondMaxLength={secondMaxLength}

          deleteArmed={deleteArmed}
          isSaveEnabled={isSaveEnabled}

          onChangeFirstField={value => {
            setFirstValue(value);
            setIsSaveEnabled((initialPopupState.firstValue !== value || initialPopupState.secondValue !== secondValue)
                             && isCurrentInitialPopupStateChosen(value, secondValue, dropDown))
            setFieldWarning(
              createWarning(value, secondValue)
            );
          }}

          onChangeSecondField={value => {
            setSecondValue(value);
            setIsSaveEnabled((initialPopupState.firstValue !== firstValue || initialPopupState.secondValue !== value)
                             && isCurrentInitialPopupStateChosen(firstValue, value, dropDown))
            setFieldWarning(
              createWarning(firstValue, value)
            );
          }}
          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={closePopup}
        />
      )}
    </section>
  );
}

export default TwoColumnView;
