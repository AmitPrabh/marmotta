/*
 * Copyright (c) 2013 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package date;

import at.newmedialab.ldpath.model.fields.FieldMapping;
import at.newmedialab.ldpath.parser.ParseException;
import at.newmedialab.ldpath.parser.RdfPathParser;
import at.newmedialab.sesame.commons.util.DateUtils;
import core.AbstractTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

public class DateFunctionsTest extends AbstractTestBase {

    private Date now;
    private Date first;
    private URI uri;
    private URI prop;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        final int delta = 60 * 60 * 24 * 365;
        now = new Date();
        first = new Date(now.getTime() - 1000l * delta);

        uri = repository.getValueFactory().createURI(NSS.get("ex") + now.getTime());
        prop = repository.getValueFactory().createURI(NSS.get("foo") + "hasPiH"); // Point in History

        final SailRepositoryConnection con = repository.getConnection();

        try {
            final ValueFactory vF = con.getValueFactory();

            con.add(vF.createStatement(uri, prop, vF.createLiteral(DateUtils.getXMLCalendar(first))));
            con.add(vF.createStatement(uri, prop, vF.createLiteral(DateUtils.getXMLCalendar(now))));

            final Random rnd = new Random();
            for (int i = 0; i < 20; i++) {
                Date d = new Date(first.getTime() + rnd.nextInt(delta) * 1000l);
                con.add(vF.createStatement(uri, prop, vF.createLiteral(DateUtils.getXMLCalendar(d))));
            }

            con.commit();
        } finally {
            con.close();
        }
    }

    @Test
    public void testEarliest() throws ParseException {
        final RdfPathParser<Value> parser = createParserFromString("fn:earliest(<" + prop.stringValue() + ">) :: xsd:dateTime");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> result = rule.getValues(backend, uri);

        Assert.assertEquals(1, result.size());

        final Object obj = result.iterator().next();
        Assert.assertEquals(first, obj);
    }

    @Test
    public void testLatest() throws ParseException {
        final RdfPathParser<Value> parser = createParserFromString("fn:latest(<" + prop.stringValue() + ">) :: xsd:dateTime");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> result = rule.getValues(backend, uri);

        Assert.assertEquals(1, result.size());

        final Object obj = result.iterator().next();
        Assert.assertEquals(now, obj);
    }

}
