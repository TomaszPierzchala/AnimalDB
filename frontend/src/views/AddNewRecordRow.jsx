function AddNewRecordRow({
	entityName,
	onCreate,
	colSpan = 2,
	acceptText = null,
	action = null
}) {
	return 	(
	<tr
	  className="empty-row"
	  onClick={onCreate}
	>
	  <td className="id-column add-icon-cell">
	    <span className={acceptText ? "add-icon accept-cell": "add-icon" }>+</span>
	  </td>

	  <td
	    colSpan={colSpan}
	    className={acceptText ? "add-text-cell add-row-main-cell accept-cell": "add-text-cell add-row-main-cell" }
	  >
		  {acceptText ?? `Click here to add a new ${entityName}...`}
	  </td>
	  {action && (
		  <td className="add-row-action-cell">
			  {action}
		  </td>
	  )}
	</tr>
	);
}

export default AddNewRecordRow;