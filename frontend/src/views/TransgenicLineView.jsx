import TwoColumnView from './TwoColumnView';
import { FIRST, SECOND } from '../utils/const';

import {
  createTransLine,
  deleteTransLine,
  getTransLines,
  updateTransLine
} from '../api/transgenicLineApi';
import { getStrains } from '../api/strainApi';

function TransgenicLineView() {
  return (
    <TwoColumnView
      entityName="Transgenic Line"

      firstName="strainCode"
      firstRequestName="strainId"
      firstEditName="strainId"

      secondName="name"
      warningKey = {FIRST | SECOND}

      firstInputType="select"
      getFirstOptionsApi={getStrains}

      createApi={createTransLine}
      getApi={getTransLines}
      updateApi={updateTransLine}
      deleteApi={deleteTransLine}
    />
  );
}

export default TransgenicLineView;
