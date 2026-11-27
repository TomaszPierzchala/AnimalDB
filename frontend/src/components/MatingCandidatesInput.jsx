import {useEffect, useRef, useState} from 'react';
import {getMatingCandidates} from '../api/miceApi';

export const SELECT_TRANSGENIC_LINE = "select : Strain / TransgenicLine";

function MatingCandidatesInput({
                                   value,
                                   sex,
                                   transgenicLineId,
                                   onChange
                               }) {
    const [candidates, setCandidates] = useState([]);
    const requestId = useRef(0);

    // Clear candidates when parameters change
    useEffect(() => {
        requestId.current++;
        setCandidates([]);
    }, [sex, transgenicLineId]);

    function handleChange(event) {
        const text = event.target.value;
        onChange(text === '' ? null : Number(text));
    }

    async function handleFocus() {
        if (transgenicLineId === 0) return;

        const id = ++requestId.current;

        try {
            const result = await getMatingCandidates(sex, transgenicLineId);

            if (id === requestId.current) {
                setCandidates(result);
            }
        } catch (error) {
            console.error('Failed to load mating candidates:', error);

            if (id === requestId.current) {
                setCandidates([]);
            }
        }
    }

    return (
        <>
            <input
                type="number"
                value={value ?? ''}
                list={`mating-${sex}-candidates`}
                onClick={event => event.stopPropagation()}
                onChange={handleChange}
                onFocus={handleFocus}
                title={transgenicLineId === 0
                    ? SELECT_TRANSGENIC_LINE + " first"
                    : undefined}
            />

            <datalist id={`mating-${sex}-candidates`}>
                {candidates.map(candidate => (
                    <option
                        key={candidate.animalNumber}
                        value={candidate.animalNumber}
                        style={{
                            color: 'darkblue',
                            fontFamily: 'monospace',
                            fontWeight: 'bold'
                        }}
                    >
                        {candidate.animalNumber} : {candidate.strainCode} - {candidate.strainName} / {candidate.name ?? '—'}
                    </option>
                ))}
            </datalist>
        </>
    );
}

export default MatingCandidatesInput;