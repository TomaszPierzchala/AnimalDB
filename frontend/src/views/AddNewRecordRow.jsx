function AddNewRecordRow({
							 entityName,
							 onCreate,
							 colSpan = 2,
							 acceptText = null,
							 cancelAction = null,
                             onSelect = null
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
                <div className="accept-container">
                    {(onSelect && cancelAction) && (
                        <select
                            className="accept-select"
                            defaultValue=""
                            title={"Select the Strain-TransgenicLine.\nThis determines the genetic line of the mating."}
                            onClick={(e) => e.stopPropagation()}
                            onChange={(e) => {
                                onSelect?.(Number(e.target.value));
                            }}
                        >
                            <option value="" disabled>select : Strain / TransgenicLine</option>
                            <option value="1">STR — Strain Name : PRG1</option>
                            <option value="2">LONG LIVE — 100 : PRG2</option>
                            <option value="3">STR — Strain Name : —</option>
                            <option value="10">LONG LIVE — 100 : —</option>
                        </select>
                    )}

                    <span>
                        {acceptText ?? `Click here to add a new ${entityName}...`}
                    </span>
                </div>
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