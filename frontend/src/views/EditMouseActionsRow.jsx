function EditMouseActionsRow({
                                 canSave,
                                 onSave,
                                 onDelete,
                                 onCancel
                             }) {
    return (
        <tr className="mouse-edit-row">

            <td
                className="mouse-edit-delete-cell"
                onClick={onDelete}
            >
                Delete
            </td>

            <td
                className={
                    canSave
                        ? "mouse-edit-save-cell"
                        : "mouse-edit-save-cell disabled-action"
                }
                onClick={canSave ? onSave : undefined}
            >
                Save
            </td>

            <td
                className="mouse-edit-cancel-cell"
                onClick={onCancel}
            >
                Cancel
            </td>

        </tr>
    );
}

export default EditMouseActionsRow;