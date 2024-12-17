import * as fs from 'fs/promises';
import Foo from "foo/foo";
import Resources from "test/resources/index";


async function getResource(filePath: string): Promise<string> {
    try {
        return await fs.readFile(filePath, 'utf8');
    } catch (err) {
        console.error('Error reading the file:', err);
        throw err;
    }
}

describe("simple", () => {
    it("should return the correct resource text from 'foo/resources'", async () => {
        const expected = await Foo.resourceText();
        expect(expected).toEqual("Hello World Resource File");
    });

    it("should return the correct resource text from 'foo/test/resources'", async () => {
        const expectedA = await getResource(Resources["test-file-a"]);
        const expectedB = await getResource(Resources["test-file-b"]);

        expect(expectedA).toEqual("Test Hello World Resource File A");
        expect(expectedB).toEqual("Test Hello World Resource File B");
    });

    it("should return the correct resource text from 'foo/test/other-files'", async () => {
        const expected = await getResource(`${process.env.OTHER_FILES}/other-file.txt`);
        expect(expected).toEqual("Other Hello World File");
    });

});