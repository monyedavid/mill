import * as fs from 'fs';
import {sortBy} from 'node_modules/lodash';
const PackageLock = require.resolve(`package-lock.json`);

(function () {
    const args = process.argv.slice(2);
    console.log(`Sorted with lodash: [${sortBy(args).join(",")}]`);

    const json = JSON.parse(fs.readFileSync(PackageLock, 'utf8'));
    console.log(json.packages['node_modules/lodash']);
})()
