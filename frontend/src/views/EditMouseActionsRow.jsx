import {useState} from 'react';

function EditMouseActionsRow({
                                 canSave,
                                 onSave,
                                 onDelete,
                                 onCancel
                             }) {
    const [deleteArmed, setDeleteArmed] = useState(false);

    function handleDelete() {
        if (!deleteArmed) {
            setDeleteArmed(true);
            return;
        }

        onDelete();
    }

    return (
        <tr className="mouse-edit-row">

            <td
                className={
                    deleteArmed
                        ? "mouse-edit-delete-cell delete-armed"
                        : "mouse-edit-delete-cell"
                }
                onClick={handleDelete}
            >
                {deleteArmed ? 'Confirm Delete' : 'Delete'}
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