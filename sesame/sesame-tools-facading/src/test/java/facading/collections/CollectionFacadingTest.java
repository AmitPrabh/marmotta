package facading.collections;

import static org.hamcrest.CoreMatchers.hasItems;

import at.newmedialab.sesame.facading.FacadingFactory;
import at.newmedialab.sesame.facading.api.Facading;
import facading.AbstractFacadingTest;
import facading.collections.model.CollectionFacade;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class CollectionFacadingTest extends AbstractFacadingTest {

    @Test
    public void testCollectionFacading() throws RepositoryException {
        final RepositoryConnection connection = repositoryRDF.getConnection();

        final Random rnd = new Random();
        final Date a, b, c, d, e, now;
        now = new Date();
        // Start 10Yrs back;
        final int tenYrsInSecs = 10 * 365 * 24 * 60 * 60;
        a = new Date(now.getTime() - tenYrsInSecs * 1000L);
        b = new Date(a.getTime() + rnd.nextInt(tenYrsInSecs) * 1000L);
        c = new Date(a.getTime() + rnd.nextInt(tenYrsInSecs) * 1000L);
        d = new Date(a.getTime() + rnd.nextInt(tenYrsInSecs) * 1000L);
        e = new Date(a.getTime() + rnd.nextInt(tenYrsInSecs) * 1000L);

        try {
            final Facading facading = FacadingFactory.createFacading(connection);

            URI uri = connection.getValueFactory().createURI("http://www.example.com/rdf/test/collections");
            CollectionFacade facade = facading.createFacade(uri, CollectionFacade.class);

            facade.setDates(Arrays.asList(a, b, c));
            Assert.assertThat(facade.getDates(), hasItems(a, b, c));

            facade.addDate(e);
            Assert.assertThat(facade.getDates(), hasItems(c, e, b, a));

            facade.setDates(Arrays.asList(a, d, now));
            Assert.assertThat(facade.getDates(), hasItems(a, d, now));
            Assert.assertThat(facade.getDates(), CoreMatchers.not(hasItems(c, e, b)));

            facade.deleteDates();
            Assert.assertEquals(facade.getDates().size(), 0);
        } finally {
            connection.close();
        }
    }

    @Test
    public void testAutorFacading() throws RepositoryException {
        final RepositoryConnection connection = repositoryRDF.getConnection();

        String a1 = UUID.randomUUID().toString(), a2 = UUID.randomUUID().toString(), a3 = UUID.randomUUID().toString();

        try {
            final Facading facading = FacadingFactory.createFacading(connection);

            URI uri = connection.getValueFactory().createURI("http://www.example.com/rdf/test/document");
            CollectionFacade facade = facading.createFacade(uri, CollectionFacade.class);

            facade.setAutors(Arrays.asList(a1, a2));

            facade.addAutor(a3);

            connection.commit();
        } finally {
            connection.close();
        }
    }

}
