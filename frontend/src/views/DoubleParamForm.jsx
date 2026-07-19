import {
  VAR_MAX_LENGTH,
  validateEmptyAndMax
} from './textValidation';

function DoubleParamForm({
  editing,
  entityName,
  firstName,
  firstValue,
  secondName,
  secondValue,
  warning,
  deleteArmed,
  hasChanges,
  onChangeWithValidation,
  onChange,
  onSubmit,
  onDelete,
  onCancel
}) {
  return (
    <div className="popup-backdrop">
      <div className="popup">
        <h2>
          {editing === null
            ? `Add ${entityName}`
            : `Edit ${entityName}`}
        </h2>

        <form onSubmit={onSubmit}>
          <div>
            <label>
              {firstName}:

              <input
                type="text"
                value={firstValue}
                onChange={(event) => {
                  const value = event.target.value;

                  onChangeWithValidation(
                    value,
                    validateEmptyAndMax(value)
                  );
                }}
                maxLength={VAR_MAX_LENGTH}
              />

              <small className="field-warning">
                {warning}
              </small>
            </label>
          </div>

          <div>
            <label>
              {secondName}:

              <input
                type="text"
                value={secondValue}
                onChange={(event) =>
                  onChange(event.target.value)
                }
              />
            </label>
          </div>

          <div className="popup-buttons">
            <div>
              {editing !== null && (
                <button
                  type="button"
                  className={
                    deleteArmed
                      ? 'delete-button delete-armed'
                      : 'delete-button'
                  }
                  onClick={onDelete}
                >
                  {deleteArmed
                    ? 'Confirm delete'
                    : 'Delete'}
                </button>
              )}
            </div>

            <div className="popup-main-buttons">
              <button
                type="submit"
                disabled={
                  editing !== null &&
                  !hasChanges
                }
              >
                {editing === null
                  ? 'Add'
                  : 'Save'}
              </button>

              <button
                type="button"
                onClick={onCancel}
              >
                Cancel
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default DoubleParamForm;
