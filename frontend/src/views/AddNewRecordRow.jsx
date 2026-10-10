function AddNewRecordRow({
							 entityName,
							 onCreate,
							 colSpan = 2,
							 acceptText = null,
							 cancelAction = null
						 }) {
	return (
		<tr className={`empty-row${acceptText ? ' accept-row' : ''}`}>

			<td
				className="id-column add-icon-cell"
				onClick={onCreate}
			>
                <span className="add-icon">
                    +
                </span>
			</td>

			<td
				colSpan={colSpan}
				className="add-text-cell"
				onClick={onCreate}
			>
				{acceptText ?? `Click here to add a new ${entityName}...`}
			</td>

			{cancelAction && (
				<td className="add-row-action-cell cancel-action"
					onClick={cancelAction}
				> Cancel </td>
			)}

		</tr>
	);
}

export default AddNewRecordRow;