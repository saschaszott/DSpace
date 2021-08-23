/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * http://www.dspace.org/license/
 */
package org.dspace.eperson.dao.impl;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.dspace.content.DSpaceObject;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Subscription;
import org.dspace.eperson.Subscription_;
import org.dspace.eperson.dao.SubscriptionDAO;

/**
 * Hibernate implementation of the Database Access Object interface class for the Subscription object.
 * This class is responsible for all database calls for the Subscription object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class SubscriptionDAOImpl extends AbstractHibernateDAO<Subscription> implements SubscriptionDAO {
    protected SubscriptionDAOImpl() {
        super();
    }

    @Override
    public List<Subscription> findByEPerson(Context context, EPerson eperson,
                                            Integer limit, Integer offset) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        javax.persistence.criteria.CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Subscription.class);
        Root<Subscription> subscriptionRoot = criteriaQuery.from(Subscription.class);
        criteriaQuery.select(subscriptionRoot);
        criteriaQuery.where(criteriaBuilder.equal(subscriptionRoot.get(Subscription_.ePerson), eperson));
        return list(context, criteriaQuery, false, Subscription.class, limit, offset);

    }

    @Override
    public List<Subscription> findByEPersonAndDso(Context context, EPerson eperson,
                                                  DSpaceObject dSpaceObject,
                                                  Integer limit, Integer offset) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        javax.persistence.criteria.CriteriaQuery criteriaQuery =
                getCriteriaQuery(criteriaBuilder, Subscription.class);
        Root<Subscription> subscriptionRoot = criteriaQuery.from(Subscription.class);
        criteriaQuery.select(subscriptionRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(
                        subscriptionRoot.get(Subscription_.ePerson), eperson),
                criteriaBuilder.equal(subscriptionRoot.get(Subscription_.dSpaceObject), dSpaceObject)
        ));
        List<javax.persistence.criteria.Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.asc(subscriptionRoot.get(Subscription_.ePerson)));
        orderList.add(criteriaBuilder.asc(subscriptionRoot.get(Subscription_.id)));
        criteriaQuery.orderBy(orderList);
        return list(context, criteriaQuery, false, Subscription.class, limit, offset);
    }


    @Override
    public void deleteByDspaceObject(Context context, DSpaceObject dSpaceObject) throws SQLException {
        String hqlQuery = "delete from Subscription where dSpaceObject=:dSpaceObject";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("dSpaceObject", dSpaceObject);
        query.executeUpdate();
    }

    @Override
    public void deleteByEPerson(Context context, EPerson eperson) throws SQLException {
        String hqlQuery = "delete from Subscription where ePerson=:ePerson";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("ePerson", eperson);
        query.executeUpdate();
    }

    @Override
    public void deleteByDSOAndEPerson(Context context, DSpaceObject dSpaceObject, EPerson eperson)
            throws SQLException {
        String hqlQuery = "delete from Subscription where dSpaceObject=:dSpaceObject AND ePerson=:ePerson";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("dSpaceObject", dSpaceObject);
        query.setParameter("ePerson", eperson);
        query.executeUpdate();
    }

    @Override
    public List<Subscription> findAllOrderedByEPersonAndResourceType(Context context, String resourceType,
                                                      Integer limit, Integer offset) throws SQLException {
        String hqlQuery = "select s from Subscription s join %s dso ON dso.id = s.dSpaceObject ORDER BY eperson_id";
        if (resourceType != null) {
            hqlQuery = String.format(hqlQuery, resourceType);
        }
        Query query = createQuery(context, hqlQuery);
        if (limit != -1) {
            query.setMaxResults(limit);
        }
        if (offset != -1) {
            query.setFirstResult(offset);
        }
        query.setHint("org.hibernate.cacheable", false);
        return query.getResultList();
    }
    @Override
    public List<Subscription> findAllOrderedByEPerson(Context context) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Subscription.class);
        Root<Subscription> subscriptionRoot = criteriaQuery.from(Subscription.class);
        criteriaQuery.select(subscriptionRoot);
        List<javax.persistence.criteria.Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.asc(subscriptionRoot.get(Subscription_.ePerson)));
        orderList.add(criteriaBuilder.asc(subscriptionRoot.get(Subscription_.id)));
        criteriaQuery.orderBy(orderList);
        return list(context, criteriaQuery, false, Subscription.class, -1, -1);
    }
}