import {
  SYMBOL_MAX_LENGTH,
  validateEmptyAndMax
} from '../textValidation';

function GeneForm({
  editingGene,
  symbol,
  description,
  symbolWarning,
  deleteArmed,
  hasChanges,
  onSymbolChange,
  onDescriptionChange,
  onSubmit,
  onDelete,
  onCancel
}) {
  return (
    <div className="popup-backdrop">
      <div className="popup">
        <h2>
          {editingGene === null ? 'Add gene' : 'Edit gene'}
        </h2>

        <form onSubmit={onSubmit}>
          <div>
            <label>
              Symbol:

              <input
                type="text"
                value={symbol}
                onChange={(event) => {
                  const value = event.target.value;

                  onSymbolChange(
                    value,
                    validateEmptyAndMax(value)
                  );
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
                onChange={(event) =>
                  onDescriptionChange(event.target.value)
                }
              />
            </label>
          </div>

          <div className="popup-buttons">
            <div>
              {editingGene !== null && (
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
                  editingGene !== null &&
                  !hasChanges
                }
              >
                {editingGene === null
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

export default GeneForm;
