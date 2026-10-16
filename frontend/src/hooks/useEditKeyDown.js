import {useEffect} from 'react';

export function useEditKeyDown({
                                   isEditing,
                                   hasChanges,
                                   isAdding,
                                   onSave,
                                   onAccept,
                                   onCancel
                               }) {
    useEffect(() => {

        function handleKeyDown(event) {

            if (event.key === 'Escape') {
                if (isEditing || isAdding) {
                    event.preventDefault();
                    onCancel();
                }

                return;
            }

            if (event.key !== 'Enter') {
                return;
            }

            if (event.target.tagName === 'TEXTAREA') {
                return;
            }

            if (isEditing && hasChanges) {
                event.preventDefault();
                onSave();
                return;
            }

            if (isAdding) {
                event.preventDefault();
                onAccept();
            }
        }

        window.addEventListener(
            'keydown',
            handleKeyDown
        );

        return () => {
            window.removeEventListener(
                'keydown',
                handleKeyDown
            );
        };

    }, [
        isEditing,
        hasChanges,
        isAdding,
        onSave,
        onAccept,
        onCancel
    ]);
}