package cotube.services;

import cotube.domain.Views;

import java.util.List;

public interface ViewsService {
    Views addView(Views view); //add views to db *C
    List<Views> getAllViews(); //get all views in db *R
    void deleteView(Views view);
}