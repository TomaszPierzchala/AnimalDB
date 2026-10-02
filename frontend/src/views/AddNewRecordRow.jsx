function AddNewRecordRow({
	entityName,
	onCreate,
	colSpan = 2,
	acceptText = null
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
	    className={acceptText ? "add-text-cell accept-cell": "add-text-cell" }
	  >
		  {acceptText ?? `Click here to add a new ${entityName}...`}
	  </td>
	</tr>
	);
}

export default AddNewRecordRow;